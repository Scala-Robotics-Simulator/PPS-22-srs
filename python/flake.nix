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
      pkgs = import nixpkgs {inherit system;};
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
        ]);
    in {
      devShell = pkgs.mkShell {
        buildInputs = [
          python
          pkgs.jupyter-all
        ];
      };
    });
}
