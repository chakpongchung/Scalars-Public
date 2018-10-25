#!/bin/bash
OS="`uname`"
ROOT="$(git rev-parse --show-toplevel)/tests";
case $OS in
  Linux*|Darwin*)
    echo "Scanner Tests";
    $ROOT/scanner/test.sh;
    echo "Parser Tests";
    $ROOT/parser/test.sh;
    echo "Hidden Scanner Tests";
    $ROOT/scanner-hidden/test.sh;
    echo "Hidden Parser Tests";
    $ROOT/parser-hidden/test.sh;
    echo "Semantic Tests";
    $ROOT/semantics/test.sh;
    echo "Semantic Hidden Tests";
    $ROOT/semantics-hidden/test.sh;
    echo "Codegen Tests";
    $ROOT/codegen/test.sh;
    echo "Codegen Hidden Tests";
    $ROOT/codegen-hidden/test.sh;
    ;;
  CYGWIN*|Windows*)
    cd $ROOT;
    echo "Scanner Tests";
    bash scanner/test.sh;
    echo "Parser Tests";
    bash parser/test.sh;
    echo "Hidden Scanner Tests";
    bash scanner-hidden/test.sh;
    echo "Hidden Parser Tests";
    bash parser-hidden/test.sh;
    echo "Semantic Tests";
    bash semantics/test.sh;
    echo "Semantic Hidden Tests";
    bash semantics-hidden/test.sh;
    echo "Codegen Tests";
    bash codegen/test.sh;
    echo "Codegen Hidden Tests";
    bash codegen-hidden/test.sh;
    ;;
  *)
    echo "Error: Unknown $OS";
    ;;
esac
