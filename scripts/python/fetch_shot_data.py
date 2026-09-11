"""
NBA Shot Data Fetcher - Production
Processes ALL players with parallel execution, batching, and error recovery.
Thread-safe Kafka publishing using thread-local producers.
"""

import json
import time
import logging
import signal
import sys
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from kafka import KafkaProducer
from nba_api.stats.endpoints import shotchartdetail
from nba_api.stats.static import players

# ==========================================
# CONFIGURATION
# ==========================================

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
KAFKA_TOPIC = 'player_shot_data'

BATCH_SIZE = 1000
MAX_WORKERS = 10
API_DELAY = 0.3
MAX_RETRIES = 3

SEASONS = ['2025-26', '2024-25', '2023-24', '2022-23', '2021-22']

shutdown_requested = False

# ==========================================
# THREAD-LOCAL KAFKA PRODUCER
# ==========================================

thread_local = threading.local()

def get_producer():
    """Get or create a thread-local Kafka producer."""
    if not hasattr(thread_local, 'producer'):
        thread_local.producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v, default=str).encode('utf-8'),
            key_serializer=lambda k: k.encode('utf-8') if k else None,
            acks=1,
            retries=5,
            batch_size=65536,
            linger_ms=50,
            max_in_flight_requests_per_connection=10,
            request_timeout_ms=30000
        )
        logger.debug(f"✅ Created producer for thread {threading.current_thread().name}")
    return thread_local.producer

def close_thread_producer():
    """Close the thread-local producer."""
    if hasattr(thread_local, 'producer'):
        try:
            thread_local.producer.flush(timeout=30)
            thread_local.producer.close(timeout=10)
        except Exception as e:
            logger.warning(f"Error closing producer: {e}")
        del thread_local.producer

# ==========================================
# GRACEFUL SHUTDOWN
# ==========================================

def signal_handler(signum, frame):
    global shutdown_requested
    logger.warning("\n⚠️ Shutdown requested. Finishing current batch...")
    shutdown_requested = True

signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)

# ==========================================
# NBA API
# ==========================================

def fetch_player_shots(player_id, season):
    """Fetch shot data with exponential backoff retry."""
    for attempt in range(MAX_RETRIES):
        try:
            data = shotchartdetail.ShotChartDetail(
                team_id=0,
                player_id=player_id,
                season_nullable=season,
                season_type_all_star='Regular Season',
                context_measure_simple='FGA'
            )
            df = data.get_data_frames()[0]
            return [] if df.empty else df.to_dict('records')
        except Exception as e:
            if attempt < MAX_RETRIES - 1:
                time.sleep(1.5 ** attempt)
                continue
            logger.debug(f"Failed {player_id}/{season}: {e}")
            return []
    return []

# ==========================================
# PUBLISHING (THREAD-SAFE)
# ==========================================

def publish_batch(producer, batch, player_id, season):
    """Publish a batch of shots."""
    try:
        for shot in batch:
            shot['PLAYER_ID'] = player_id
            shot['SEASON_ID'] = season
            producer.send(KAFKA_TOPIC, key=str(player_id), value=shot)
        return len(batch)
    except Exception as e:
        logger.error(f"Batch publish error: {e}")
        return 0

def publish_shots(producer, shots, player_id, season):
    """Publish shots in batches."""
    total = 0
    for i in range(0, len(shots), BATCH_SIZE):
        batch = shots[i:i + BATCH_SIZE]
        total += publish_batch(producer, batch, player_id, season)
    return total

# ==========================================
# PLAYER PROCESSING
# ==========================================

def process_player(player, seasons):
    """Process all seasons for one player using a thread-local producer."""
    if shutdown_requested:
        return None
    
    producer = get_producer()  # ← Each thread gets its own producer
    player_id = player['id']
    player_name = player['full_name']
    total = 0
    successful_seasons = 0
    
    for season in seasons:
        if shutdown_requested:
            break
        shots = fetch_player_shots(player_id, season)
        if shots:
            published = publish_shots(producer, shots, player_id, season)
            total += published
            if published > 0:
                successful_seasons += 1
        time.sleep(API_DELAY)
    
    # Flush this thread's producer at the end
    try:
        producer.flush(timeout=30)
    except Exception as e:
        logger.warning(f"Flush error for {player_name}: {e}")
    
    return {
        'name': player_name,
        'total': total,
        'seasons': successful_seasons
    }

# ==========================================
# MAIN
# ==========================================

def main():
    print("=" * 60)
    print("🏀 NBA SHOT DATA FETCHER - THREAD-SAFE")
    print("=" * 60)
    
    start_time = time.time()
    
    # 1. Get all active players
    logger.info("📋 Fetching active players...")
    active_players = [p for p in players.get_players() if p.get('is_active')]
    logger.info(f"✅ Found {len(active_players)} active players")
    
    # 2. Config summary
    print(f"\n⚙️  Configuration:")
    print(f"   Players:  {len(active_players)}")
    print(f"   Seasons:  {len(SEASONS)} ({', '.join(SEASONS)})")
    print(f"   Workers:  {MAX_WORKERS}")
    print(f"   Batch:    {BATCH_SIZE}")
    
    # 3. Process all players
    print("\n" + "=" * 60)
    print("📊 PROCESSING ALL PLAYERS")
    print("=" * 60)
    
    total_shots = 0
    total_players = 0
    failed_players = []
    empty_players = []
    completed = 0
    total_count = len(active_players)
    
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        future_map = {
            executor.submit(process_player, player, SEASONS): player
            for player in active_players
        }
        
        try:
            for future in as_completed(future_map):
                if shutdown_requested:
                    break
                completed += 1
                player = future_map[future]
                
                try:
                    result = future.result()
                    if result is None:
                        continue
                    
                    total_players += 1
                    total_shots += result['total']
                    
                    if result['total'] > 0:
                        print(f"  [{completed}/{total_count}] ✅ {result['name']}: "
                              f"{result['total']:,} shots ({result['seasons']} seasons)")
                    else:
                        empty_players.append(result['name'])
                        print(f"  [{completed}/{total_count}] ⏭️  {result['name']}: No shots")
                        
                except Exception as e:
                    failed_players.append(player['full_name'])
                    print(f"  [{completed}/{total_count}] ❌ {player['full_name']}: {e}")
        
        except KeyboardInterrupt:
            logger.warning("⚠️ Interrupted by user")
    
    # 4. Final summary
    elapsed = time.time() - start_time
    print("\n" + "=" * 60)
    print("📊 FINAL SUMMARY")
    print("=" * 60)
    print(f"  ✅ Players processed:     {total_players:,}")
    print(f"  📤 Total shots published: {total_shots:,}")
    print(f"  ⏭️  Players with no shots: {len(empty_players)}")
    print(f"  ❌ Players with errors:   {len(failed_players)}")
    if empty_players:
        print(f"  Empty sample: {', '.join(empty_players[:5])}")
    if failed_players:
        print(f"  Failed sample: {', '.join(failed_players[:10])}")
    print(f"  ⏱️  Total time:           {elapsed / 60:.1f} minutes")
    print(f"  🚀 Throughput:            {total_shots / max(elapsed, 1):.0f} shots/sec")
    print("=" * 60)
    print("✅ Done!")

if __name__ == "__main__":
    main()