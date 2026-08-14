"""
NBA Advanced Metrics Fetcher
Fetches advanced stats from LeagueDashPlayerStats endpoint.
Only fetches metrics that are actually available.
"""

import json
import os
import sys
import time
import logging
from kafka import KafkaProducer
from nba_api.stats.endpoints import leaguedashplayerstats

# ==========================================
# LOGGING CONFIGURATION
# ==========================================

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# ==========================================
# KAFKA CONFIGURATION
# ==========================================

KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
KAFKA_TOPIC = 'player_advanced_metrics'

# ==========================================
# KAFKA PRODUCER
# ==========================================

def create_kafka_producer():
    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v, default=str).encode('utf-8'),
            key_serializer=lambda k: k.encode('utf-8') if k else None,
            acks='all',
            retries=3,
            max_in_flight_requests_per_connection=1
        )
        logger.info(f"✅ Connected to Kafka at {KAFKA_BOOTSTRAP_SERVERS}")
        return producer
    except Exception as e:
        logger.error(f"❌ Failed to connect to Kafka: {e}")
        return None

# ==========================================
# NBA API FUNCTIONS
# ==========================================

def fetch_advanced_metrics(season_id='2025-26'):
    """
    Fetch advanced metrics for all players for a given season.
    Only fetches metrics that are actually available.
    """
    try:
        logger.info(f"📊 Fetching advanced metrics for season: {season_id}")
        
        stats = leaguedashplayerstats.LeagueDashPlayerStats(
            season=season_id,
            season_type_all_star='Regular Season',
            measure_type_detailed_defense='Advanced',
            per_mode_detailed='PerGame'
        )
        
        df = stats.get_data_frames()[0]
        records = df.to_dict('records')
        
        # ✅ Only keep metrics that are actually available
        filtered_records = []
        for record in records:
            filtered_record = {
                'PLAYER_ID': record.get('PLAYER_ID'),
                'PLAYER_NAME': record.get('PLAYER_NAME'),
                'GP': record.get('GP'),
                'USG_PCT': record.get('USG_PCT'),
                'TS_PCT': record.get('TS_PCT'),
                'AST_PCT': record.get('AST_PCT'),
                'REB_PCT': record.get('REB_PCT'),
                'EFG_PCT': record.get('EFG_PCT'),
                'TOV_PCT': record.get('TOV_PCT'),
                'FTA_RATE': record.get('FTA_RATE'),
            }
            filtered_records.append(filtered_record)
        
        logger.info(f"✅ Fetched {len(filtered_records)} player records")
        return filtered_records
        
    except Exception as e:
        logger.error(f"❌ Error fetching advanced metrics: {e}")
        return []

# ==========================================
# MAIN FUNCTION
# ==========================================

def main():
    logger.info("🏀 Starting NBA Advanced Metrics Fetcher")
    logger.info("=" * 50)
    
    # 1. Connect to Kafka
    producer = create_kafka_producer()
    if not producer:
        logger.error("❌ Kafka unavailable. Exiting.")
        sys.exit(1)
    
    # 2. Fetch data for all seasons
    seasons = ['2025-26', '2024-25', '2023-24', '2022-23', '2021-22']
    all_records = []
    
    logger.info(f"\n📅 Fetching data for seasons: {seasons}")
    
    for season in seasons:
        records = fetch_advanced_metrics(season)
        
        if records:
            for record in records:
                record['SEASON_ID'] = season
            all_records.extend(records)
            logger.info(f"✅ Season {season}: {len(records)} records")
        else:
            logger.warning(f"❌ No records for season {season}")
        
        time.sleep(1)
    
    if not all_records:
        logger.error("❌ No records fetched. Exiting.")
        sys.exit(1)
    
    # 3. Publish to Kafka
    logger.info(f"\n📤 Publishing {len(all_records)} records to Kafka...")
    
    kafka_success_count = 0
    
    for idx, record in enumerate(all_records):
        player_id = record.get('PLAYER_ID')
        
        if not player_id:
            continue
        
        try:
            key = str(player_id)
            future = producer.send(KAFKA_TOPIC, key=key, value=record)
            future.get(timeout=10)
            kafka_success_count += 1
            
            if idx < 5:
                logger.info(f"\n📤 Sample record {idx+1}:")
                logger.info(f"   PLAYER_ID: {record.get('PLAYER_ID')}")
                logger.info(f"   PLAYER_NAME: {record.get('PLAYER_NAME')}")
                logger.info(f"   SEASON_ID: {record.get('SEASON_ID')}")
                logger.info(f"   GP: {record.get('GP')}")
                logger.info(f"   USG_PCT: {record.get('USG_PCT')}")
                logger.info(f"   TS_PCT: {record.get('TS_PCT')}")
                logger.info(f"   AST_PCT: {record.get('AST_PCT')}")
                logger.info(f"   REB_PCT: {record.get('REB_PCT')}")
                logger.info(f"   EFG_PCT: {record.get('EFG_PCT')}")
                
        except Exception as e:
            logger.error(f"❌ Failed to publish: {e}")
        
        time.sleep(0.05)
    
    producer.flush()
    
    # 4. Summary
    print("\n" + "=" * 50)
    print("📊 FINAL SUMMARY")
    print(f"  ✅ Published to Kafka: {kafka_success_count}")
    print(f"  📤 Topic: {KAFKA_TOPIC}")
    print("\n✅ Done!")

if __name__ == "__main__":
    main()