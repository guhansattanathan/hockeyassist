from nba_api.stats.endpoints import playercareerstats, commonplayerinfo
from nba_api.stats.static import players
from kafka import KafkaProducer
import json
import os
import time
import sys
import logging

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Get the project root directory
script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(os.path.dirname(script_dir))

# Save to data/ folder at project root
data_dir = os.path.join(project_root, 'data')
os.makedirs(data_dir, exist_ok=True)  # ← This ensures the directory exists

# ==========================================
# KAFKA CONFIGURATION
# ==========================================

KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
KAFKA_TOPIC = 'player-stats'

def create_kafka_producer():
    """Create and return a Kafka producer"""
    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v, default=str).encode('utf-8'),
            key_serializer=lambda k: k.encode('utf-8') if k else None,
            acks='all',  # Wait for all replicas to acknowledge
            retries=3,
            max_in_flight_requests_per_connection=1  # Preserve ordering
        )
        logger.info(f"✅ Connected to Kafka at {KAFKA_BOOTSTRAP_SERVERS}")
        return producer
    except Exception as e:
        logger.error(f"❌ Failed to connect to Kafka: {e}")
        return None

# ==========================================
# NBA API FUNCTIONS
# ==========================================

def get_all_active_players():
    """Get all active NBA players"""
    try:
        all_players = players.get_players()
        active_players = [p for p in all_players if p.get('is_active')]
        logger.info(f"Found {len(active_players)} active players")
        return active_players
    except Exception as e:
        logger.error(f"Error fetching player list: {e}")
        return []

def fetch_player_stats(player_id):
    """Fetch career stats for a single player"""
    try:
        career_stats = playercareerstats.PlayerCareerStats(player_id=player_id)
        return career_stats.get_dict()
    except Exception as e:
        logger.warning(f"Error fetching stats for player {player_id}: {e}")
        return None

def fetch_player_info(player_id):
    """Fetch additional player info (team, position, etc.)"""
    try:
        info = commonplayerinfo.CommonPlayerInfo(player_id=player_id)
        info_dict = info.get_dict()
        
        for result_set in info_dict.get('resultSets', []):
            if result_set.get('name') == 'CommonPlayerInfo':
                rows = result_set.get('rowSet', [])
                if rows and len(rows) > 0:
                    row = rows[0]
                    return {
                        'position': row[15] if len(row) > 15 else None,
                        'team': row[18] if len(row) > 18 else None,
                        'team_name': row[17] if len(row) > 17 else None,
                        'jersey': row[14] if len(row) > 14 else None,
                        'height': row[11] if len(row) > 11 else None,
                        'weight': row[12] if len(row) > 12 else None,
                        'experience': row[13] if len(row) > 13 else None,
                        'college': row[9] if len(row) > 9 else None,
                    }
    except Exception as e:
        logger.warning(f"Error fetching info for player {player_id}: {e}")
        return None
    return None

def save_to_json(data, filename):
    """Fallback: Save data to JSON file if Kafka is unavailable"""
    output_file = os.path.join(data_dir, filename)
    with open(output_file, 'w') as file:
        json.dump(data, file, indent=2, default=str)
    logger.info(f"💾 Saved to {output_file}")

# ==========================================
# MAIN FUNCTION
# ==========================================

def main():
    logger.info("🏀 Starting NBA data fetch with Kafka...")
    logger.info("=" * 50)
    
    # 1. Connect to Kafka
    producer = create_kafka_producer()
    if not producer:
        logger.warning("⚠️ Kafka unavailable. Falling back to JSON file storage.")
        use_kafka = False
    else:
        use_kafka = True
        logger.info("✅ Kafka connected. Will publish messages to topic: " + KAFKA_TOPIC)
    
    # 2. Get all active players
    logger.info("📋 Fetching list of active players...")
    active_players = get_all_active_players()
    if not active_players:
        logger.error("No active players found. Exiting.")
        sys.exit(1)
    
    # 3. Fetch stats for each player
    all_players_data = []
    failed_players = []
    successful_count = 0
    kafka_success_count = 0
    
    # Limit for testing - set to None for all players
    MAX_PLAYERS = None  # Set to e.g., 50 for testing
    
    players_to_fetch = active_players[:MAX_PLAYERS] if MAX_PLAYERS else active_players
    
    logger.info(f"\n📊 Processing {len(players_to_fetch)} players...")
    
    for idx, player in enumerate(players_to_fetch, 1):
        player_id = player['id']
        player_name = player['full_name']
        
        print(f"  [{idx}/{len(players_to_fetch)}] {player_name}...", end=" ")
        sys.stdout.flush()
        
        # Fetch career stats
        stats = fetch_player_stats(player_id)
        
        # Fetch additional player info (team, position)
        player_info = fetch_player_info(player_id)
        
        if stats and player_info:
            # Build the complete message
            message = {
                'player_info': {
                    'id': player['id'],
                    'full_name': player['full_name'],
                    'first_name': player['first_name'],
                    'last_name': player['last_name'],
                    'is_active': player['is_active'],
                    'position': player_info.get('position'),
                    'team': player_info.get('team'),
                    'team_name': player_info.get('team_name'),
                    'jersey': player_info.get('jersey'),
                    'height': player_info.get('height'),
                    'weight': player_info.get('weight'),
                    'experience': player_info.get('experience'),
                    'college': player_info.get('college')
                },
                'stats': stats  # The full career stats from NBA API
            }
            
            # Option A: Publish to Kafka
            if use_kafka:
                try:
                    # Use player_id as the key for partitioning
                    key = str(player_id)
                    future = producer.send(KAFKA_TOPIC, key=key, value=message)
                    # Wait for acknowledgment (optional)
                    record_metadata = future.get(timeout=10)
                    kafka_success_count += 1
                    print("✅ (Kafka)")
                except Exception as e:
                    logger.error(f"  ❌ Kafka error for {player_name}: {e}")
                    # Fallback: save to JSON if Kafka fails
                    all_players_data.append(message)
                    print("✅ (Saved to JSON fallback)")
            else:
                # Option B: Save to JSON (fallback)
                all_players_data.append(message)
                print("✅ (JSON)")
            
            successful_count += 1
        else:
            failed_players.append(player_name)
            print("❌")
        
        # Rate limiting - NBA API doesn't like too many requests
        time.sleep(0.5)
    
    # 4. Flush any remaining Kafka messages
    if use_kafka:
        producer.flush()
        logger.info(f"📤 Published {kafka_success_count} messages to Kafka topic '{KAFKA_TOPIC}'")
    
    # 5. Save fallback JSON file (for players that failed Kafka or if Kafka was unavailable)
    if all_players_data:
        save_to_json(all_players_data, 'all_players_data_fallback.json')
    
    # 6. Summary
    print("\n" + "=" * 50)
    print("📊 SUMMARY")
    print(f"  ✅ Total players processed: {successful_count}")
    if use_kafka:
        print(f"  📤 Published to Kafka: {kafka_success_count}")
    else:
        print(f"  💾 Saved to JSON: {len(all_players_data)}")
    print(f"  ❌ Failed players: {len(failed_players)}")
    if failed_players:
        print(f"  Failed: {', '.join(failed_players[:10])}")
        if len(failed_players) > 10:
            print(f"  ... and {len(failed_players) - 10} more")
    
    print("\n✅ Done!")

if __name__ == "__main__":
    main()