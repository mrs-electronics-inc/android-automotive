{
  description = "Dev shell for Android Automotive board and demo app workflows";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    android-nixpkgs.url = "github:tadfisher/android-nixpkgs";
  };

  outputs = { self, nixpkgs, android-nixpkgs }:
    let
      system = "x86_64-linux"; # adjust if needed
      pkgs = import nixpkgs {
        inherit system;
        config.allowUnfree = true;
        overlays = [
          (final: _prev: {
            android-sdk = android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
              build-tools-35-0-0
              cmdline-tools-latest
              platform-tools
              platforms-android-34
            ]);
          })
        ];
      };
    in {
      devShells.${system}.default = pkgs.mkShell {
        packages = with pkgs; [
          android-tools
          android-sdk
          jdk17_headless
          just
          uuu
        ];

        shellHook = ''
          export JAVA_HOME="${pkgs.jdk17_headless}"
          export ANDROID_HOME="${pkgs.android-sdk}/share/android-sdk"
          export ANDROID_SDK_ROOT="${pkgs.android-sdk}/share/android-sdk"
          export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=${pkgs.android-sdk}/share/android-sdk/build-tools/35.0.0/aapt2"
          echo "Dev shell loaded: adb, fastboot, uuu, just, and JDK 17 available"
        '';
      };
    };
}
