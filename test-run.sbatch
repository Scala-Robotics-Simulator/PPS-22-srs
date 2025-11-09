#!/bin/bash
#SBATCH --job-name=sceredi-test-run
#SBATCH --mail-type=ALL
#SBATCH --mail-user=simone.ceredi@studio.unibo.it
#SBATCH --time=24:00:00
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=2
#SBATCH --mem=10G
#SBATCH --partition=l40
#SBATCH --output=test-run.log
#SBATCH --chdir=/scratch.hpc/simone.ceredi/test-run
#SBATCH --gres=gpu:1
PATH=$PATH:/scratch.hpc/simone.ceredi/jdk-21.0.9/bin
java -jar PPS-22-srs-assembly-latest.jar --rl --port 50051 > /dev/null &
sleep 10s
source venv/bin/activate
cd PPS-22-SRS/python
./create_proto_definitions.sh
cd src/scripts
python3 train-dqagent.py --neurons 64 32 --config-root envs --checkpoint-dir checkpoints oa --episodes 10000 --steps 5000 --env oa --window-size 100 --port 50051

