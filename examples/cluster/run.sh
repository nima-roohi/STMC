# Run this file using the following command from terminal:
# ./examples/cluster/run.sh > ./examples/cluster/res 2>&1

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

run_example()
{
  repeat=20
  echo "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
  time timeout 30m $PRISM $1 -pf $2 -mtbdd    | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
  time timeout 30m $PRISM $1 -pf $2 -sparse   | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
  time timeout 30m $PRISM $1 -pf $2 -hybrid   | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
  time timeout 30m $PRISM $1 -pf $2 -explicit | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
  #time timeout 30m $PRISM $1 -pf $2 -exact    | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo

  echo 'PRISM SPRT'
  $PRISM $1 -pf $2 -sim -simmethod sprt                                     -repeat $repeat -mt 1                 -simconf $3 -simwidth $4                                        | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'GLRT'
  $PRISM $1 -pf $2 -sim -stmc -smp_method independent -hyp_test_method SPRT -repeat $repeat -mt 1 -min_iter 100   -alpha $3 -beta $3 -delta $4                                    | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'antithetic'
  $PRISM $1 -pf $2 -sim -stmc -smp_method antithetic -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 50   -alpha $3 -beta $3 -delta $4                                     | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'strata-size 1'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 100  -alpha $3 -beta $3 -delta $4 -strata_size 1                      | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'strata-size 2'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 50  -alpha $3 -beta $3 -delta $4 -strata_size 2                       | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'strata-size 16'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 10  -alpha $3 -beta $3 -delta $4 -strata_size 2,2,2,2                 | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 10  -alpha $3 -beta $3 -delta $4 -strata_size 4,4                     | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 10  -alpha $3 -beta $3 -delta $4 -strata_size 16                      | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'strata-size 256'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 2,2,2,2,2,2,2,2          | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 4,4,4,4                  | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 16,16                    | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 256                      | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'strata-size 4096'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 2  -alpha $3 -beta $3 -delta $4 -strata_size 2,2,2,2,2,2,2,2,2,2,2,2  | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 2  -alpha $3 -beta $3 -delta $4 -strata_size 4,4,4,4,4,4              | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 2  -alpha $3 -beta $3 -delta $4 -strata_size 16,16,16                 | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 2  -alpha $3 -beta $3 -delta $4 -strata_size 64,64                    | grep -E 'Result:|seconds|Time: average|Samples: average'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 2  -alpha $3 -beta $3 -delta $4 -strata_size 4096                     | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
}

run_example "./examples/cluster/cluster.sm -const N=512"   "P<0.12[\"minimum\"U<=200(!\"minimum\"|!\"premium\")]" 0.0001 0.001
run_example "./examples/cluster/cluster.sm -const N=2048"  "P<0.12[\"minimum\"U<=200(!\"minimum\"|!\"premium\")]" 0.0001 0.001
run_example "./examples/cluster/cluster.sm -const N=4096"  "P<0.12[\"minimum\"U<=200(!\"minimum\"|!\"premium\")]" 0.0001 0.001
run_example "./examples/cluster/cluster.sm -const N=8192"  "P<0.12[\"minimum\"U<=200(!\"minimum\"|!\"premium\")]" 0.0001 0.001


