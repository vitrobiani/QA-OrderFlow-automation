{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    jdk17
    maven
    chromium
    chromedriver
  ];

  JAVA_HOME = "${pkgs.jdk17}";
  CHROME_BIN = "${pkgs.chromium}/bin/chromium";

  shellHook = ''
    echo "OrderFlow Automation Environment"
    echo "Java: $(java -version 2>&1 | head -1)"
    echo "Maven: $(mvn -version 2>&1 | head -1)"
    echo "Chromium: $CHROME_BIN"
    echo ""
    echo "Run tests with: mvn test"
    echo "Run single test: mvn -q -Dtest=Test142_CategorySelectionCold test"
  '';
}
