from nba_api.stats.endpoints import playercareerstats, commonplayerinfo
from nba_api.stats.static import players
import json
import os
import time
import sys

# Get the project root directory
script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(os.path.dirname(script_dir))

# Save to data/ folder at project root
data_dir = os.path.join(project_root, 'data')
os.makedirs(data_dir, exist_ok=True)

def get_all_active_players():
    """Get all active NBA players"""
    all_players = players.get_players()
    active_players = [p for p in all_players if p.get('is_active')]
    return active_players

def fetch_player_stats(player_id):
    """Fetch career stats for a single player"""
    try:
        career_stats = playercareerstats.PlayerCareerStats(player_id=player_id)
        return career_stats.get_dict()
    except Exception as e:
        print(f"  ❌ Error fetching stats for player {player_id}: {e}")
        return None

def fetch_player_info(player_id):
    """Fetch additional player info (team, position, etc.)"""
    try:
        info = commonplayerinfo.CommonPlayerInfo(player_id=player_id)
        info_dict = info.get_dict()
        
        # Extract from the result sets
        for result_set in info_dict.get('resultSets', []):
            if result_set.get('name') == 'CommonPlayerInfo':
                rows = result_set.get('rowSet', [])
                if rows and len(rows) > 0:
                    row = rows[0]
                    # The columns are: PLAYER_ID, FIRST_NAME, LAST_NAME, DISPLAY_FIRST_LAST, 
                    # DISPLAY_LAST_COMMA_FIRST, DISPLAY_FI_LAST, PLAYER_SLUG, BIRTHDATE, 
                    # SCHOOL, COUNTRY, LAST_AFFILIATION, HEIGHT, WEIGHT, SEASON_EXP, 
                    # JERSEY, POSITION, ROSTERSTATUS, TEAM_ID, TEAM_NAME, TEAM_ABBREVIATION, 
                    # TEAM_CODE, TEAM_CITY, PLAYERCODE, FROM_YEAR, TO_YEAR, DRAFT_YEAR, 
                    # DRAFT_ROUND, DRAFT_NUMBER, GREATEST_75_FLAG
                    return {
                        'position': row[15] if len(row) > 15 else None,  # POSITION
                        'team': row[18] if len(row) > 18 else None,       # TEAM_ABBREVIATION
                        'team_name': row[17] if len(row) > 17 else None,  # TEAM_NAME
                        'jersey': row[14] if len(row) > 14 else None,     # JERSEY
                        'height': row[11] if len(row) > 11 else None,     # HEIGHT
                        'weight': row[12] if len(row) > 12 else None,     # WEIGHT
                        'experience': row[13] if len(row) > 13 else None, # SEASON_EXP
                        'college': row[9] if len(row) > 9 else None,      # SCHOOL
                    }
    except Exception as e:
        print(f"  ❌ Error fetching info for player {player_id}: {e}")
        return None
    
    return None

def save_to_json(data, filename):
    """Save data to JSON file"""
    output_file = os.path.join(data_dir, filename)
    with open(output_file, 'w') as file:
        json.dump(data, file, indent=2)
    print(f"  💾 Saved to {output_file}")

def main():
    print("🏀 Fetching NBA player data...")
    print("=" * 50)
    
    # 1. Get all active players
    print("📋 Fetching list of active players...")
    active_players = get_all_active_players()
    print(f"✅ Found {len(active_players)} active players")
    
    # 2. Fetch stats for each player
    all_players_data = []
    failed_players = []
    
    # Limit for testing - set to None for all players
    MAX_PLAYERS = None  # Set to 50 for testing, None for all
    
    players_to_fetch = active_players[:MAX_PLAYERS] if MAX_PLAYERS else active_players
    
    print(f"\n📊 Fetching stats for {len(players_to_fetch)} players...")
    
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
            # Add player info to the data
            stats['player_info'] = {
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
            }
            all_players_data.append(stats)
            print("✅")
        else:
            failed_players.append(player_name)
            print("❌")
        
        # Rate limiting - NBA API doesn't like too many requests
        time.sleep(0.5)
    
    # 3. Save all players to a single file
    print("\n" + "=" * 50)
    print(f"📦 Saving data for {len(all_players_data)} players...")
    
    # Save all players
    save_to_json(all_players_data, 'all_players_data.json')
    
    # 4. Summary
    print("\n" + "=" * 50)
    print("📊 SUMMARY")
    print(f"  ✅ Total players fetched: {len(all_players_data)}")
    print(f"  ❌ Failed players: {len(failed_players)}")
    if failed_players:
        print(f"  Failed: {', '.join(failed_players[:10])}")
        if len(failed_players) > 10:
            print(f"  ... and {len(failed_players) - 10} more")
    
    print("\n✅ Done!")

if __name__ == "__main__":
    main()