{
  description = "Karasu Lab Android app development environment";

  inputs.nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";

  outputs =
    { nixpkgs, ... }:
    let
      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];
      forAllSystems = f: nixpkgs.lib.genAttrs systems (system: f nixpkgs.legacyPackages.${system});
    in
    {
      devShells = forAllSystems (
        pkgs:
        let
          isDarwin = pkgs.stdenv.hostPlatform.isDarwin;
          pinentry = if isDarwin then pkgs.pinentry_mac else pkgs.pinentry-gnome3;
          pinentryProgram = if isDarwin then "${pinentry}/Applications/pinentry-mac.app/Contents/MacOS/pinentry-mac" else "${pinentry}/bin/pinentry";
        in
        {
          default = pkgs.mkShell {
            packages = [
              pkgs.gnupg
              pkgs.nodejs
              pkgs.pnpm
              pinentry
            ];
            env.PINENTRY_PROGRAM = pinentryProgram;
          };
        }
      );
    };
}
