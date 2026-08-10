from nba_api.stats.static import teams
from nba_api.stats.endpoints import teaminfocommon
import json
import os
import time
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
os.makedirs(data_dir, exist_ok=True)

# ==========================================
# NBA API FUNCTIONS
# ==========================================

def get_all_nba_teams():
    """Get all NBA teams"""
    try:
        all_teams = teams.get_teams()
        logger.info(f"Found {len(all_teams)} NBA teams")
        return all_teams
    except Exception as e:
        logger.error(f"Error fetching team list: {e}")
        return []

def fetch_team_info(team_id):
    """Fetch additional team info (arena, colors, etc.)"""
    try:
        team_info = teaminfocommon.TeamInfoCommon(team_id=team_id)
        info_dict = team_info.get_dict()
        
        for result_set in info_dict.get('resultSets', []):
            if result_set.get('name') == 'TeamInfoCommon':
                rows = result_set.get('rowSet', [])
                if rows and len(rows) > 0:
                    row = rows[0]
                    return {
                        'arena': row[6] if len(row) > 6 else None,
                        'arena_city': row[7] if len(row) > 7 else None,
                        'arena_state': row[8] if len(row) > 8 else None,
                        'conference': row[10] if len(row) > 10 else None,
                        'division': row[11] if len(row) > 11 else None,
                    }
    except Exception as e:
        logger.warning(f"Error fetching info for team {team_id}: {e}")
        return None
    return None

def save_to_json(data, filename):
    """Save data to JSON file"""
    output_file = os.path.join(data_dir, filename)
    with open(output_file, 'w') as file:
        json.dump(data, file, indent=2, default=str)
    logger.info(f"💾 Saved to {output_file}")

# ==========================================
# MAIN FUNCTION
# ==========================================

def main():
    logger.info("🏀 Starting NBA team data fetch...")
    logger.info("=" * 50)
    
    # 1. Get all teams
    logger.info("📋 Fetching list of NBA teams...")
    all_teams = get_all_nba_teams()
    if not all_teams:
        logger.error("No teams found. Exiting.")
        return
    
    # 2. Fetch full team data
    team_data = []
    failed_teams = []
    
    logger.info(f"\n📊 Processing {len(all_teams)} teams...")
    
    for idx, team in enumerate(all_teams, 1):
        team_id = team['id']
        team_name = team['full_name']
        
        print(f"  [{idx}/{len(all_teams)}] {team_name}...", end=" ")
        
        # Fetch additional team info
        team_info = fetch_team_info(team_id)
        
        if team_info:
            # Build complete team record
            team_record = {
                'team_id': team['id'],
                'full_name': team['full_name'],
                'abbreviation': team['abbreviation'],
                'city': team.get('city'),
                'state': team.get('state'),
                'nickname': team.get('nickname'),
                'conference': team_info.get('conference'),
                'division': team_info.get('division'),
                'arena': team_info.get('arena'),
                'arena_city': team_info.get('arena_city'),
                'arena_state': team_info.get('arena_state'),
            }
            team_data.append(team_record)
            print("✅")
        else:
            # Still save basic info even if team_info fails
            team_record = {
                'team_id': team['id'],
                'full_name': team['full_name'],
                'abbreviation': team['abbreviation'],
                'city': team.get('city'),
                'state': team.get('state'),
                'nickname': team.get('nickname'),
                'conference': None,
                'division': None,
                'arena': None,
                'arena_city': None,
                'arena_state': None,
            }
            team_data.append(team_record)
            failed_teams.append(team_name)
            print("⚠️ (basic info only)")
        
        # Rate limiting
        time.sleep(0.2)
    
    # 3. Save to JSON
    save_to_json(team_data, 'all_teams_data.json')
    
    # 4. Also save a simplified mapping for frontend
    team_mapping = []
    for team in team_data:
        team_mapping.append({
            'id': team['team_id'],
            'full_name': team['full_name'],
            'abbreviation': team['abbreviation'],
        })
    save_to_json(team_mapping, 'team_mapping.json')
    
    # 5. Summary
    print("\n" + "=" * 50)
    print("📊 SUMMARY")
    print(f"  ✅ Total teams fetched: {len(team_data)}")
    if failed_teams:
        print(f"  ⚠️ Teams with limited data: {len(failed_teams)}")
        print(f"  Failed: {', '.join(failed_teams)}")
    
    print("\n✅ Done!")

if __name__ == "__main__":
    main()