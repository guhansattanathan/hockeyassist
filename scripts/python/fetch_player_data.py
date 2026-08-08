from nba_api.stats.endpoints import playercareerstats
from nba_api.stats.static import players
import json
import os

# Get the project root directory (parent of scripts/python)
script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(os.path.dirname(script_dir))

# Save to data/ folder at project root
data_dir = os.path.join(project_root, 'data')
os.makedirs(data_dir, exist_ok=True)

# Fetch player data (example: Nikola Jokic)
player_id = '203999'

# 1. Get player info (name, etc.)
player_info = None
all_players = players.get_players()
for player in all_players:
    if str(player['id']) == player_id:
        player_info = player
        break

if not player_info:
    print(f"Player with ID {player_id} not found")
    exit(1)

print(f"Fetching stats for: {player_info['full_name']} (ID: {player_id})")

# 2. Get career stats
career_stats = playercareerstats.PlayerCareerStats(player_id=player_id)
stats_dict = career_stats.get_dict()

# 3. Add player info to the data
stats_dict['player_info'] = {
    'id': player_info['id'],
    'full_name': player_info['full_name'],
    'first_name': player_info['first_name'],
    'last_name': player_info['last_name'],
    'is_active': player_info['is_active']
}

# 4. Save to data/player_data.json
output_file = os.path.join(data_dir, 'player_data.json')
with open(output_file, 'w') as file:
    json.dump(stats_dict, file, indent=2)

print(f"Data saved to {output_file}")
print(f"Player: {player_info['full_name']}")
print(f"Seasons: {len(stats_dict['resultSets'][0]['rowSet'])}")