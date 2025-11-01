{
  inputs = {
    nixpkgs = {url = "github:nixos/nixpkgs/nixos-unstable";};
    flake-utils = {url = "github:numtide/flake-utils";};
  };
  outputs = {
    nixpkgs,
    flake-utils,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (system: let
      pkgs = import nixpkgs {
        inherit system;
        config.allowUnfree = true;
        config.cudaSupport = true;
      };
      python = pkgs.python313.withPackages (ps:
        with ps; [
          grpcio
          grpcio-tools
          numpy
          matplotlib
          gymnasium
          tqdm
          tensorflow
          imageio
          pygame
          notebook
          jupyter
          jupyterlab
          pyzmq
          nest-asyncio
          keras
        ]);
    in {
      devShell = pkgs.mkShell {
        buildInputs = [
          python
          pkgs.jupyter-all
          pkgs.cudaPackages.cudatoolkit
          pkgs.cudaPackages.cudnn
        ];
        shellHook = ''
          export CUDA_PATH=${pkgs.cudaPackages.cudatoolkit}
          export LD_LIBRARY_PATH=${pkgs.cudaPackages.cudatoolkit}/lib:${pkgs.cudaPackages.cudnn}/lib:${pkgs.linuxPackages.nvidia_x11}/lib:$LD_LIBRARY_PATH
          export EXTRA_LDFLAGS="-L${pkgs.cudaPackages.cudatoolkit}/lib -L${pkgs.cudaPackages.cudnn}/lib"
          export EXTRA_CCFLAGS="-I${pkgs.cudaPackages.cudatoolkit}/include -I${pkgs.cudaPackages.cudnn}/include"
        '';
      };
    });
}
