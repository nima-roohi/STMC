if [[ -z "${PRISM_HOME}" ]]; then
  PRISM_HOME="/opt/prism-4.5"
fi

if [[ -z "${JAVA_HOME}" ]]; then
  JAVA_CMD="java"
else
  JAVA_CMD="${JAVA_HOME}/bin/java"
fi

export DYLD_LIBRARY_PATH=${PRISM_HOME}/lib
export LD_LIBRARY_PATH=${PRISM_HOME}/lib

PRISM="${JAVA_CMD} -Xmx1g -Xss4M -Djava.library.path=${PRISM_HOME}/lib -classpath ./out/production/stmc:./out/artifacts/stmc/stmc.jar:${PRISM_HOME}/lib/prism.jar:${PRISM_HOME}/classes:${PRISM_HOME}:${PRISM_HOME}/lib/pepa.zip:${PRISM_HOME}/lib/* edu.stmc.Main"

$PRISM "$@"
