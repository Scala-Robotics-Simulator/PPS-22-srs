# How to run trainings on the institutional servers

## Stuff to do only once

### SSH into the server

The server in which we can use the scheduler is `giano.cs.unibo.it`.

Connect to it with:
```bash
ssh nome.cognome@giano.cs.unibo.it
```

Say `yes` when prompted for stuff (`yes/no/fingerprint`).

### Install magic wormhole to easily send stuff from and to the server

1. Create a venv dedicated to magic wormhole in a easily reachable place, e.g. `~/womhole`

```bash
virtualenv wormhole
~/wormhole/bin/pip3 install --no-cache-dir magic-wormhole
```

2. To use magic wormhole you don't need to activate the venv, you can just use

```bash
~/wormhole/bin/wormhole [send/receive/...]
```

### Scratch personal directory creation

Since 400MB of space aren't enough due to python dependecies create your personal scratch directory.

```bash
mkdir -p /scratch.hpc/nome.cognome
cd /scratch.hpc/nome.cognome
```

### Download jdk 21

```bash
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz
tar xvf jdk-21_linux-x64_bin.tar.gz
```

## Stuff needed for every run

### Go to your scratch directory

```bash
cd /scratch.hpc/nome.cognome
```

### Create a directory dedicated to that particular run, maybe with documentation as well :)

```bash
mkdir dql-oa-high-lr-run-1
cd dql-oa-high-lr-run-1
```

With a nice `README.md` containing all that sweet sweet information regarding the run (e.g. purpose, whats different, ...).
There are both `vim` or `nano` so no excuses.


#### Clone the repository

Only the branch needed and with depth 1 as we don't want the entire history.

```bash
git clone https://github.com/Scala-Robotics-Simulator/PPS-22-srs -b feature/my-beautiful-feature --depth 1
```

#### Send from your local machine to the server the correct jar and the environments directory

```bash
wormhole send PPS-22-srs-assembly[...].jar
```

And receive it from the server

```bash
~/wormhole/bin/wormhole receive [very nice randomly generated name]
```

#### Create the venv to execute the python script

```bash
virtualenv venv
venv/bin/pip3 install --no-cache-dir -e PPS-22-srs/python/
```

#### Create the sbatch script
<!-- TODO: when ready with a working script update this -->

```bash
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
```

Save this file to `[name].sbatch`.

Send it for execution, AFTER CHECKING EVERYTHING IS RIGHT, with:

```bash
sbatch [name].sbatch
```

