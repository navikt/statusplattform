{
  description = "A Nix-flake based Kotlin development environment";

  inputs = {
    flake-utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "github:NixOS/nixpkgs";
  };

  outputs = {self, ...} @ inputs:
    inputs.flake-utils.lib.eachDefaultSystem (system: let
      inherit (inputs.nixpkgs) lib;
      pkgs = import inputs.nixpkgs {
        localSystem = {inherit system;};

        config.allowUnfreePredicate = pkg:
          builtins.elem (lib.getName pkg) [
            "idea-ultimate"
          ];

        overlays = [
          (final: prev: let
            javaVersion = 21;
          in {
            # JVM related overlays
            jdk = pkgs."jdk${builtins.toString javaVersion}";
            maven = prev.maven.override {
              inherit (pkgs) jdk;
            };
            jetbrains.idea-ultimate = prev.jetbrains.idea-ultimate.override {
              inherit (pkgs) jdk maven;
            };
          })
        ];
      };
    in {
      checks = {
        # inherit # Comment in when you want tests to run on every new shell
        #   <nix derivation running builds/tests/lints/etc>
        #   ;
      };
      devShells.default = pkgs.mkShell {
        packages = with pkgs; [
          # project's code specific
          ## Backend
          maven

          # Editor stuffs
          helix
          jdt-language-server
          jetbrains.idea-ultimate

          # Other tooling
          docker-compose
          postgresql_12 # psql, pg_restore, pg_dump, etc.
          pgcli # psql w/tab completion and syntax highlighting
        ];

        shellHook = ''
          ${pkgs.helix}/bin/hx --version
          ${pkgs.helix}/bin/hx --health java
          ${pkgs.maven}/bin/mvn --version
        '';
      };
      packages = {
        # docker = pkgs.dockerTools.buildImage {
        #   name = pname;
        #   tag = "v${cargo-details.package.version}";
        #   extraCommands = ''mkdir -p data'';
        #   config = {
        #     Cmd = "--help";
        #     Entrypoint = ["${cargo-package}/bin/${pname}"];
        #   };
        # };
      };
      # packages.default = cargo-package;

      # Now `nix fmt` works!
      formatter = pkgs.alejandra;
    });
}
