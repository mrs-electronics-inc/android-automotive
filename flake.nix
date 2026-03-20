{
  description = "Dev shell with fastboot (android-tools) and uuu";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux"; # adjust if needed
      pkgs = import nixpkgs {
        inherit system;
      };
    in {
      devShells.${system}.default = pkgs.mkShell {
        packages = with pkgs; [
          android-tools
          uuu
        ];

        shellHook = ''
          echo "Dev shell loaded: fastboot + uuu available"
        '';
      };
    };
}
