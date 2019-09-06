# Run this file using the following command from terminal:
# nohup ./examples/brp/run.sh > ./examples/brp/res &

export DYLD_LIBRARY_PATH=/opt/prism-4.5/lib
export LD_LIBRARY_PATH=/opt/prism-4.5/lib

PRISM="java -Xmx1g -Xss4M -Djava.library.path=/opt/prism-4.5/lib -classpath ./out/artifacts/stmc/stmc.jar:/opt/prism-4.5/lib/prism.jar:/opt/prism-4.5/classes:/opt/prism-4.5:/opt/prism-4.5/lib/pepa.zip:/opt/prism-4.5/lib/* edu.stmc.Main"

run_example()
{
  repeat=50
  echo "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
#  timeout 30m $PRISM $1 -pf $2 -simwidth $3 -simconf $4 -mtbdd    | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
#  timeout 30m $PRISM $1 -pf $2 -simwidth $3 -simconf $4 -sparse   | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
#  timeout 30m $PRISM $1 -pf $2 -simwidth $3 -simconf $4 -hybrid   | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
#  timeout 30m $PRISM $1 -pf $2 -simwidth $3 -simconf $4 -explicit | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
#  timeout 30m $PRISM $1 -pf $2 -simwidth $3 -simconf $4 -exact    | grep -e Result -e Engine -e States -e Transitions -e Error -e "Time for model" ; echo
#
#  echo 'PRISM SPRT'
#  $PRISM $1 -pf $2 -sim -simmethod sprt -simconf $3 -simwidth $4 -repeat $repeat            -mt 1                                                                                 | grep -E 'Result:|seconds|Time: average|Samples: average'
#
#  echo
#  echo 'STMC SPRT'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method independent -hyp_test_method SPRT -repeat $repeat -mt 1                 -alpha $3 -beta $3 -delta $4                                    | grep -E 'Result:|seconds|Time: average|Samples: average'
#
#  echo
#  echo 'GLRT'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method independent -hyp_test_method SPRT -repeat $repeat -mt 1 -min_iter 100   -alpha $3 -beta $3 -delta $4                                    | grep -E 'Result:|seconds|Time: average|Samples: average'
#
#  echo
#  echo 'antithetic'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method antithetic -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 50   -alpha $3 -beta $3 -delta $4                                     | grep -E 'Result:|seconds|Time: average|Samples: average'
#
#  echo
#  echo 'strata-size 1'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 100  -alpha $3 -beta $3 -delta $4 -strata_size 1                      | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo
  echo 'strata-size 2'
  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 10 -min_iter 50  -alpha $3 -beta $3 -delta $4 -strata_size 2                       | grep -E 'Result:|seconds|Time: average|Samples: average'
#
#  echo
#  echo 'strata-size 16'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 10  -alpha $3 -beta $3 -delta $4 -strata_size 2,2,2,2                 | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 10  -alpha $3 -beta $3 -delta $4 -strata_size 4,4                     | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 10  -alpha $3 -beta $3 -delta $4 -strata_size 16                      | grep -E 'Result:|seconds|Time: average|Samples: average'
#
#  echo
#  echo 'strata-size 256'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 2,2,2,2,2,2,2,2          | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 4,4,4,4                  | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 16,16                    | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 256                      | grep -E 'Result:|seconds|Time: average|Samples: average'
#
#  echo
#  echo 'strata-size 4096'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 2,2,2,2,2,2,2,2,2,2,2,2  | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 4,4,4,4,4,4              | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 16,16,16                 | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 64,64                    | grep -E 'Result:|seconds|Time: average|Samples: average'
#  $PRISM $1 -pf $2 -sim -stmc -smp_method stratified -hyp_test_method SSPRT -repeat $repeat -mt 1 -min_iter 5  -alpha $3 -beta $3 -delta $4 -strata_size 4096                     | grep -E 'Result:|seconds|Time: average|Samples: average'

  echo "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-="
}

run_example "./examples/brp/brp.pm -const MAX=15  -const N=4096"  "P<0.39[F<100s=3]" 0.001 0.001
#run_example "./examples/brp/brp.pm -const MAX=20  -const N=8192"  "P<0.39[F<100s=3]" 0.001 0.001
#run_example "./examples/brp/brp.pm -const MAX=64  -const N=16384" "P<0.39[F<100s=3]" 0.001 0.001
#run_example "./examples/brp/brp.pm -const MAX=256 -const N=65536" "P<0.39[F<100s=3]" 0.001 0.001


