find ./generated/phototaxis/conf -type f -name "*.yml" -exec sed -i \
  -e 's/reward: .*/reward: Phototaxis/' \
  -e 's/termination: .*/termination: LightReached/' \
  -e 's/truncation: .*/truncation: CollisionContact/' \
  -e 's/illuminationRadius: .*/illuminationRadius: 6.0/' \
  {} +
