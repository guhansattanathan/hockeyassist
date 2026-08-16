"""
NBA Shot Data Fetcher - Ultra Optimized
Processes ALL players with parallel execution and batch publishing.
"""

import json
import time
import logging
from concurrent.futures import ThreadPoolExecutor, as_completed
from kafka import KafkaProducer
from nba_api.stats.endpoints import shotchartdetail
from nba_api.stats.static import players

# ==========================================
# CONFIGURATION
# ==========================================

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
KAFKA_TOPIC = 'player_shot_data'
BATCH_SIZE = 500
MAX_WORKERS = 5
SEASONS = ['2025-26', '2024-25']  # Start with 2 seasons for faster testing

# ==========================================
# KAFKA PRODUCER
# ==========================================

def create_producer():
    """Create a high-performance Kafka producer."""
    try:
        return KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v, default=str).encode('utf-8'),
            key_serializer=lambda k: k.encode('utf-8') if k else None,
            acks=1,
            retries=2,
            compression_type='snappy',
            batch_size=32768,
            linger_ms=100,
            max_in_flight_requests_per_connection=5
        )
    except Exception as e:
        logger.error(f"Failed to create producer: {e}")
        return None

# ==========================================
# NBA API FUNCTIONS
# ==========================================

def fetch_player_shots(player_id, season):
    """Fetch shot data for a player-season with retry logic."""
    for attempt in range(3):
        try:
            data = shotchartdetail.ShotChartDetail(
                team_id=0,
                player_id=player_id,
                season_nullable=season,
                season_type_all_star='Regular Season',
                context_measure_simple='FGA'
            )
            df = data.get_data_frames()[0]
            if df.empty:
                return []
            return df.to_dict('records')
        except Exception as e:
            if attempt < 2:
                time.sleep(1)
                continue
            logger.debug(f"Error for {player_id} in {season}: {e}")
            return []
    return []

def publish_batch(producer, batch, player_id, season):
    """Publish a batch of shots."""
    try:
        for shot in batch:
            shot['PLAYER_ID'] = player_id
            shot['SEASON_ID'] = season
            producer.send(KAFKA_TOPIC, key=str(player_id), value=shot)
        return len(batch)
    except Exception as e:
        logger.error(f"Batch publish failed: {e}")
        return 0

def publish_shots(producer, shots, player_id, season):
    """Publish shots in batches."""
    total = 0
    for i in range(0, len(shots), BATCH_SIZE):
        batch = shots[i:i+BATCH_SIZE]
        total += publish_batch(producer, batch, player_id, season)
    return total

def process_player(player, seasons):
    """Process a single player across all seasons."""
    player_id = player['id']
    player_name = player['full_name']
    total_shots = 0
    failed_seasons = []
    
    for season in seasons:
        try:
            shots = fetch_player_shots(player_id, season)
            if shots:
                published = publish_shots(producer, shots, player_id, season)
                total_shots += published
            else:
                failed_seasons.append(season)
        except Exception as e:
            logger.error(f"Error processing {player_name} - {season}: {e}")
            failed_seasons.append(season)
    
    return {
        'name': player_name,
        'id': player_id,
        'total': total_shots,
        'seasons': len(seasons) - len(failed_seasons),
        'failed_seasons': failed_seasons
    }

# ==========================================
# MAIN FUNCTION
# ==========================================

def main():
    print("=" * 60)
    print("🏀 NBA SHOT DATA FETCHER - OPTIMIZED")
    print("=" * 60)
    
    start_time = time.time()
    
    # 1. Create Kafka producer
    global producer
    producer = create_producer()
    if not producer:
        logger.error("❌ Kafka unavailable. Exiting.")
        return
    
    logger.info(f"✅ Connected to Kafka at {KAFKA_BOOTSTRAP_SERVERS}")
    
    # 2. Get all active players
    logger.info("📋 Fetching active players...")
    all_players = players.get_players()
    active_players = [p for p in all_players if p.get('is_active')]
    logger.info(f"✅ Found {len(active_players)} active players")
    
    # ⚠️ Limit for testing - change to None for all players
    MAX_PLAYERS = 50  # Start with 50, change to None for all
    players_to_process = active_players[:MAX_PLAYERS] if MAX_PLAYERS else active_players
    
    logger.info(f"📅 Seasons: {SEASONS}")
    logger.info(f"👥 Players: {len(players_to_process)}")
    logger.info(f"⚡ Workers: {MAX_WORKERS} concurrent")
    
    # 3. Process players
    print("\n" + "=" * 60)
    print("📊 PROCESSING PLAYERS")
    print("=" * 60)
    
    total_shots = 0
    total_players = 0
    failed_players = []
    
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        future_to_player = {
            executor.submit(process_player, player, SEASONS): player['full_name']
            for player in players_to_process
        }
        
        completed = 0
        total_players_count = len(players_to_process)
        
        for future in as_completed(future_to_player):
            completed += 1
            name = future_to_player[future]
            try:
                result = future.result()
                total_players += 1
                total_shots += result['total']
                
                if result['total'] > 0:
                    status = "✅"
                    if result['failed_seasons']:
                        status = "⚠️"
                    print(f"  [{completed}/{total_players_count}] {status} {name}: {result['total']:,} shots ({result['seasons']} seasons)")
                else:
                    failed_players.append(name)
                    print(f"  [{completed}/{total_players_count}] ❌ {name}: No data")
                
            except Exception as e:
                failed_players.append(name)
                print(f"  [{completed}/{total_players_count}] ❌ {name}: Error - {e}")
    
    # 4. Flush
    producer.flush()
    producer.close()
    
    # 5. Summary
    elapsed = time.time() - start_time
    print("\n" + "=" * 60)
    print("📊 FINAL SUMMARY")
    print("=" * 60)
    print(f"  ✅ Players processed: {total_players}")
    print(f"  📤 Total shots published: {total_shots:,}")
    print(f"  ❌ Players with no data: {len(failed_players)}")
    if failed_players:
        print(f"  Failed: {', '.join(failed_players[:10])}")
        if len(failed_players) > 10:
            print(f"  ... and {len(failed_players) - 10} more")
    print(f"  ⏱️ Time: {elapsed / 60:.1f} minutes")
    print("=" * 60)
    print("✅ Done!")

if __name__ == "__main__":
    main()