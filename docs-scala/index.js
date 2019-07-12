Index.PACKAGES = {"edu" : [], "edu.stmc" : [{"name" : "edu.stmc.HypTest", "shortDescription" : "Same as SimulationMethod, with a few default implementations and a few more functions that help testing the class without using Sampler orExpression.", "members_class" : [{"label" : "getResult", "tail" : "(sampler: Sampler): AnyRef", "member" : "edu.stmc.HypTest.getResult", "link" : "edu\/stmc\/HypTest.html#getResult(sampler:simulator.sampler.Sampler):AnyRef", "kind" : "def"}, {"label" : "getProgress", "tail" : "(iters: Int, sampler: Sampler): Int", "member" : "edu.stmc.HypTest.getProgress", "link" : "edu\/stmc\/HypTest.html#getProgress(iters:Int,sampler:simulator.sampler.Sampler):Int", "kind" : "def"}, {"label" : "computeMissingParameterAfterSim", "tail" : "(): Unit", "member" : "edu.stmc.HypTest.computeMissingParameterAfterSim", "link" : "edu\/stmc\/HypTest.html#computeMissingParameterAfterSim():Unit", "kind" : "def"}, {"label" : "computeMissingParameterBeforeSim", "tail" : "(): Unit", "member" : "edu.stmc.HypTest.computeMissingParameterBeforeSim", "link" : "edu\/stmc\/HypTest.html#computeMissingParameterBeforeSim():Unit", "kind" : "def"}, {"member" : "edu.stmc.HypTest#<init>", "error" : "unsupported entity"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "edu\/stmc\/HypTest.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "edu\/stmc\/HypTest.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "edu\/stmc\/HypTest.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "edu\/stmc\/HypTest.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "edu\/stmc\/HypTest.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "edu\/stmc\/HypTest.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "edu\/stmc\/HypTest.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTest.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTest.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTest.html#wait():Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "edu\/stmc\/HypTest.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "edu\/stmc\/HypTest.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "edu\/stmc\/HypTest.html#toString():String", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "edu\/stmc\/HypTest.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "edu\/stmc\/HypTest.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "edu\/stmc\/HypTest.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "edu\/stmc\/HypTest.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "edu\/stmc\/HypTest.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}, {"label" : "failed_to_reject", "tail" : "(): Boolean", "member" : "edu.stmc.HypTest.failed_to_reject", "link" : "edu\/stmc\/HypTest.html#failed_to_reject:Boolean", "kind" : "abstract def"}, {"label" : "rejected", "tail" : "(): Boolean", "member" : "edu.stmc.HypTest.rejected", "link" : "edu\/stmc\/HypTest.html#rejected:Boolean", "kind" : "abstract def"}, {"label" : "too_close", "tail" : "(): Boolean", "member" : "edu.stmc.HypTest.too_close", "link" : "edu\/stmc\/HypTest.html#too_close:Boolean", "kind" : "abstract def"}, {"label" : "completed", "tail" : "(): Boolean", "member" : "edu.stmc.HypTest.completed", "link" : "edu\/stmc\/HypTest.html#completed:Boolean", "kind" : "abstract def"}, {"label" : "update", "tail" : "(positive: Int, negative: Int): Unit", "member" : "edu.stmc.HypTest.update", "link" : "edu\/stmc\/HypTest.html#update(positive:Int,negative:Int):Unit", "kind" : "abstract def"}, {"label" : "update", "tail" : "(positive: Boolean): Unit", "member" : "edu.stmc.HypTest.update", "link" : "edu\/stmc\/HypTest.html#update(positive:Boolean):Unit", "kind" : "abstract def"}, {"label" : "getResultExplanation", "tail" : "(sampler: Sampler): String", "member" : "edu.stmc.HypTest.getResultExplanation", "link" : "edu\/stmc\/HypTest.html#getResultExplanation(sampler:simulator.sampler.Sampler):String", "kind" : "abstract def"}, {"label" : "shouldStopNow", "tail" : "(iters: Int, sampler: Sampler): Boolean", "member" : "edu.stmc.HypTest.shouldStopNow", "link" : "edu\/stmc\/HypTest.html#shouldStopNow(iters:Int,sampler:simulator.sampler.Sampler):Boolean", "kind" : "abstract def"}, {"label" : "getParametersString", "tail" : "(): String", "member" : "edu.stmc.HypTest.getParametersString", "link" : "edu\/stmc\/HypTest.html#getParametersString():String", "kind" : "abstract def"}, {"label" : "getMissingParameter", "tail" : "(): AnyRef", "member" : "edu.stmc.HypTest.getMissingParameter", "link" : "edu\/stmc\/HypTest.html#getMissingParameter():AnyRef", "kind" : "abstract def"}, {"label" : "setExpression", "tail" : "(expr: Expression): Unit", "member" : "edu.stmc.HypTest.setExpression", "link" : "edu\/stmc\/HypTest.html#setExpression(expr:parser.ast.Expression):Unit", "kind" : "abstract def"}, {"label" : "reset", "tail" : "(): Unit", "member" : "edu.stmc.HypTest.reset", "link" : "edu\/stmc\/HypTest.html#reset():Unit", "kind" : "abstract def"}, {"label" : "getFullName", "tail" : "(): String", "member" : "edu.stmc.HypTest.getFullName", "link" : "edu\/stmc\/HypTest.html#getFullName():String", "kind" : "abstract def"}, {"label" : "getName", "tail" : "(): String", "member" : "edu.stmc.HypTest.getName", "link" : "edu\/stmc\/HypTest.html#getName():String", "kind" : "abstract def"}, {"label" : "clone", "tail" : "(): SimulationMethod", "member" : "simulator.method.SimulationMethod.clone", "link" : "edu\/stmc\/HypTest.html#clone():simulator.method.SimulationMethod", "kind" : "abstract def"}], "class" : "edu\/stmc\/HypTest.html", "kind" : "class"}, {"name" : "edu.stmc.HypTestSPRT", "shortDescription" : "Sequential Probability Ratio Test", "members_class" : [{"label" : "failed_to_reject", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestSPRT.failed_to_reject", "link" : "edu\/stmc\/HypTestSPRT.html#failed_to_reject:Boolean", "kind" : "def"}, {"label" : "rejected", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestSPRT.rejected", "link" : "edu\/stmc\/HypTestSPRT.html#rejected:Boolean", "kind" : "def"}, {"label" : "too_close", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestSPRT.too_close", "link" : "edu\/stmc\/HypTestSPRT.html#too_close:Boolean", "kind" : "def"}, {"label" : "completed", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestSPRT.completed", "link" : "edu\/stmc\/HypTestSPRT.html#completed:Boolean", "kind" : "def"}, {"label" : "status", "tail" : "(): Binary", "member" : "edu.stmc.HypTestSPRT.status", "link" : "edu\/stmc\/HypTestSPRT.html#status:edu.stmc.CompResult.Binary", "kind" : "def"}, {"label" : "status", "tail" : "(logT: Double): Binary", "member" : "edu.stmc.HypTestSPRT.status", "link" : "edu\/stmc\/HypTestSPRT.html#status(logT:Double):edu.stmc.CompResult.Binary", "kind" : "def"}, {"label" : "update", "tail" : "(positive: Int, negative: Int): Unit", "member" : "edu.stmc.HypTestSPRT.update", "link" : "edu\/stmc\/HypTestSPRT.html#update(positive:Int,negative:Int):Unit", "kind" : "def"}, {"label" : "update", "tail" : "(positive: Boolean): Unit", "member" : "edu.stmc.HypTestSPRT.update", "link" : "edu\/stmc\/HypTestSPRT.html#update(positive:Boolean):Unit", "kind" : "def"}, {"label" : "getMissingParameter", "tail" : "(): Integer", "member" : "edu.stmc.HypTestSPRT.getMissingParameter", "link" : "edu\/stmc\/HypTestSPRT.html#getMissingParameter():Integer", "kind" : "def"}, {"label" : "shouldStopNow", "tail" : "(iters: Int, sampler: Sampler): Boolean", "member" : "edu.stmc.HypTestSPRT.shouldStopNow", "link" : "edu\/stmc\/HypTestSPRT.html#shouldStopNow(iters:Int,sampler:simulator.sampler.Sampler):Boolean", "kind" : "def"}, {"label" : "setExpression", "tail" : "(expr: Expression): Unit", "member" : "edu.stmc.HypTestSPRT.setExpression", "link" : "edu\/stmc\/HypTestSPRT.html#setExpression(expr:parser.ast.Expression):Unit", "kind" : "def"}, {"label" : "clone", "tail" : "(): HypTestSPRT", "member" : "edu.stmc.HypTestSPRT.clone", "link" : "edu\/stmc\/HypTestSPRT.html#clone():edu.stmc.HypTestSPRT", "kind" : "def"}, {"label" : "getResultExplanation", "tail" : "(sampler: Sampler): String", "member" : "edu.stmc.HypTestSPRT.getResultExplanation", "link" : "edu\/stmc\/HypTestSPRT.html#getResultExplanation(sampler:simulator.sampler.Sampler):String", "kind" : "def"}, {"label" : "getParametersString", "tail" : "(): String", "member" : "edu.stmc.HypTestSPRT.getParametersString", "link" : "edu\/stmc\/HypTestSPRT.html#getParametersString():String", "kind" : "def"}, {"label" : "getFullName", "tail" : "(): String", "member" : "edu.stmc.HypTestSPRT.getFullName", "link" : "edu\/stmc\/HypTestSPRT.html#getFullName():String", "kind" : "def"}, {"label" : "getName", "tail" : "(): String", "member" : "edu.stmc.HypTestSPRT.getName", "link" : "edu\/stmc\/HypTestSPRT.html#getName():String", "kind" : "def"}, {"label" : "reset", "tail" : "(): Unit", "member" : "edu.stmc.HypTestSPRT.reset", "link" : "edu\/stmc\/HypTestSPRT.html#reset():Unit", "kind" : "def"}, {"label" : "init", "tail" : "(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean): HypTestSPRT", "member" : "edu.stmc.HypTestSPRT.init", "link" : "edu\/stmc\/HypTestSPRT.html#init(threshold:Double,alpha:Double,beta:Double,delta:Double,LB:Boolean):edu.stmc.HypTestSPRT", "kind" : "def"}, {"member" : "edu.stmc.HypTestSPRT#<init>", "error" : "unsupported entity"}, {"label" : "getResult", "tail" : "(sampler: Sampler): AnyRef", "member" : "edu.stmc.HypTest.getResult", "link" : "edu\/stmc\/HypTestSPRT.html#getResult(sampler:simulator.sampler.Sampler):AnyRef", "kind" : "def"}, {"label" : "getProgress", "tail" : "(iters: Int, sampler: Sampler): Int", "member" : "edu.stmc.HypTest.getProgress", "link" : "edu\/stmc\/HypTestSPRT.html#getProgress(iters:Int,sampler:simulator.sampler.Sampler):Int", "kind" : "def"}, {"label" : "computeMissingParameterAfterSim", "tail" : "(): Unit", "member" : "edu.stmc.HypTest.computeMissingParameterAfterSim", "link" : "edu\/stmc\/HypTestSPRT.html#computeMissingParameterAfterSim():Unit", "kind" : "def"}, {"label" : "computeMissingParameterBeforeSim", "tail" : "(): Unit", "member" : "edu.stmc.HypTest.computeMissingParameterBeforeSim", "link" : "edu\/stmc\/HypTestSPRT.html#computeMissingParameterBeforeSim():Unit", "kind" : "def"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "edu\/stmc\/HypTestSPRT.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "edu\/stmc\/HypTestSPRT.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "edu\/stmc\/HypTestSPRT.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "edu\/stmc\/HypTestSPRT.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "edu\/stmc\/HypTestSPRT.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "edu\/stmc\/HypTestSPRT.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "edu\/stmc\/HypTestSPRT.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTestSPRT.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTestSPRT.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTestSPRT.html#wait():Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "edu\/stmc\/HypTestSPRT.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "edu\/stmc\/HypTestSPRT.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "edu\/stmc\/HypTestSPRT.html#toString():String", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "edu\/stmc\/HypTestSPRT.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "edu\/stmc\/HypTestSPRT.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "edu\/stmc\/HypTestSPRT.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "edu\/stmc\/HypTestSPRT.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "edu\/stmc\/HypTestSPRT.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "class" : "edu\/stmc\/HypTestSPRT.html", "kind" : "class"}, {"name" : "edu.stmc.HypTestTSPRT", "shortDescription" : "Ternary Sequential Probability Ratio Test", "members_class" : [{"label" : "failed_to_reject", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestTSPRT.failed_to_reject", "link" : "edu\/stmc\/HypTestTSPRT.html#failed_to_reject:Boolean", "kind" : "def"}, {"label" : "rejected", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestTSPRT.rejected", "link" : "edu\/stmc\/HypTestTSPRT.html#rejected:Boolean", "kind" : "def"}, {"label" : "too_close", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestTSPRT.too_close", "link" : "edu\/stmc\/HypTestTSPRT.html#too_close:Boolean", "kind" : "def"}, {"label" : "completed", "tail" : "(): Boolean", "member" : "edu.stmc.HypTestTSPRT.completed", "link" : "edu\/stmc\/HypTestTSPRT.html#completed:Boolean", "kind" : "def"}, {"label" : "status", "tail" : "(): Ternary", "member" : "edu.stmc.HypTestTSPRT.status", "link" : "edu\/stmc\/HypTestTSPRT.html#status:edu.stmc.CompResult.Ternary", "kind" : "def"}, {"label" : "status", "tail" : "(lb: Binary, ub: Binary): Ternary", "member" : "edu.stmc.HypTestTSPRT.status", "link" : "edu\/stmc\/HypTestTSPRT.html#status(lb:edu.stmc.CompResult.Binary,ub:edu.stmc.CompResult.Binary):edu.stmc.CompResult.Ternary", "kind" : "def"}, {"label" : "update", "tail" : "(positive: Int, negative: Int): Unit", "member" : "edu.stmc.HypTestTSPRT.update", "link" : "edu\/stmc\/HypTestTSPRT.html#update(positive:Int,negative:Int):Unit", "kind" : "def"}, {"label" : "update", "tail" : "(positive: Boolean): Unit", "member" : "edu.stmc.HypTestTSPRT.update", "link" : "edu\/stmc\/HypTestTSPRT.html#update(positive:Boolean):Unit", "kind" : "def"}, {"label" : "getMissingParameter", "tail" : "(): Integer", "member" : "edu.stmc.HypTestTSPRT.getMissingParameter", "link" : "edu\/stmc\/HypTestTSPRT.html#getMissingParameter():Integer", "kind" : "def"}, {"label" : "shouldStopNow", "tail" : "(iters: Int, sampler: Sampler): Boolean", "member" : "edu.stmc.HypTestTSPRT.shouldStopNow", "link" : "edu\/stmc\/HypTestTSPRT.html#shouldStopNow(iters:Int,sampler:simulator.sampler.Sampler):Boolean", "kind" : "def"}, {"label" : "setExpression", "tail" : "(expr: Expression): Unit", "member" : "edu.stmc.HypTestTSPRT.setExpression", "link" : "edu\/stmc\/HypTestTSPRT.html#setExpression(expr:parser.ast.Expression):Unit", "kind" : "def"}, {"label" : "clone", "tail" : "(): HypTestTSPRT", "member" : "edu.stmc.HypTestTSPRT.clone", "link" : "edu\/stmc\/HypTestTSPRT.html#clone():edu.stmc.HypTestTSPRT", "kind" : "def"}, {"label" : "getResultExplanation", "tail" : "(sampler: Sampler): String", "member" : "edu.stmc.HypTestTSPRT.getResultExplanation", "link" : "edu\/stmc\/HypTestTSPRT.html#getResultExplanation(sampler:simulator.sampler.Sampler):String", "kind" : "def"}, {"label" : "getParametersString", "tail" : "(): String", "member" : "edu.stmc.HypTestTSPRT.getParametersString", "link" : "edu\/stmc\/HypTestTSPRT.html#getParametersString():String", "kind" : "def"}, {"label" : "getFullName", "tail" : "(): String", "member" : "edu.stmc.HypTestTSPRT.getFullName", "link" : "edu\/stmc\/HypTestTSPRT.html#getFullName():String", "kind" : "def"}, {"label" : "getName", "tail" : "(): String", "member" : "edu.stmc.HypTestTSPRT.getName", "link" : "edu\/stmc\/HypTestTSPRT.html#getName():String", "kind" : "def"}, {"label" : "reset", "tail" : "(): Unit", "member" : "edu.stmc.HypTestTSPRT.reset", "link" : "edu\/stmc\/HypTestTSPRT.html#reset():Unit", "kind" : "def"}, {"label" : "init", "tail" : "(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double, LB: Boolean): HypTestTSPRT", "member" : "edu.stmc.HypTestTSPRT.init", "link" : "edu\/stmc\/HypTestTSPRT.html#init(threshold:Double,alpha:Double,beta:Double,gamma:Double,delta:Double,LB:Boolean):edu.stmc.HypTestTSPRT", "kind" : "def"}, {"member" : "edu.stmc.HypTestTSPRT#<init>", "error" : "unsupported entity"}, {"label" : "getResult", "tail" : "(sampler: Sampler): AnyRef", "member" : "edu.stmc.HypTest.getResult", "link" : "edu\/stmc\/HypTestTSPRT.html#getResult(sampler:simulator.sampler.Sampler):AnyRef", "kind" : "def"}, {"label" : "getProgress", "tail" : "(iters: Int, sampler: Sampler): Int", "member" : "edu.stmc.HypTest.getProgress", "link" : "edu\/stmc\/HypTestTSPRT.html#getProgress(iters:Int,sampler:simulator.sampler.Sampler):Int", "kind" : "def"}, {"label" : "computeMissingParameterAfterSim", "tail" : "(): Unit", "member" : "edu.stmc.HypTest.computeMissingParameterAfterSim", "link" : "edu\/stmc\/HypTestTSPRT.html#computeMissingParameterAfterSim():Unit", "kind" : "def"}, {"label" : "computeMissingParameterBeforeSim", "tail" : "(): Unit", "member" : "edu.stmc.HypTest.computeMissingParameterBeforeSim", "link" : "edu\/stmc\/HypTestTSPRT.html#computeMissingParameterBeforeSim():Unit", "kind" : "def"}, {"label" : "synchronized", "tail" : "(arg0: ⇒ T0): T0", "member" : "scala.AnyRef.synchronized", "link" : "edu\/stmc\/HypTestTSPRT.html#synchronized[T0](x$1:=>T0):T0", "kind" : "final def"}, {"label" : "##", "tail" : "(): Int", "member" : "scala.AnyRef.##", "link" : "edu\/stmc\/HypTestTSPRT.html###():Int", "kind" : "final def"}, {"label" : "!=", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.!=", "link" : "edu\/stmc\/HypTestTSPRT.html#!=(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "==", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.==", "link" : "edu\/stmc\/HypTestTSPRT.html#==(x$1:Any):Boolean", "kind" : "final def"}, {"label" : "ne", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.ne", "link" : "edu\/stmc\/HypTestTSPRT.html#ne(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "eq", "tail" : "(arg0: AnyRef): Boolean", "member" : "scala.AnyRef.eq", "link" : "edu\/stmc\/HypTestTSPRT.html#eq(x$1:AnyRef):Boolean", "kind" : "final def"}, {"label" : "finalize", "tail" : "(): Unit", "member" : "scala.AnyRef.finalize", "link" : "edu\/stmc\/HypTestTSPRT.html#finalize():Unit", "kind" : "def"}, {"label" : "wait", "tail" : "(arg0: Long, arg1: Int): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTestTSPRT.html#wait(x$1:Long,x$2:Int):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(arg0: Long): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTestTSPRT.html#wait(x$1:Long):Unit", "kind" : "final def"}, {"label" : "wait", "tail" : "(): Unit", "member" : "scala.AnyRef.wait", "link" : "edu\/stmc\/HypTestTSPRT.html#wait():Unit", "kind" : "final def"}, {"label" : "notifyAll", "tail" : "(): Unit", "member" : "scala.AnyRef.notifyAll", "link" : "edu\/stmc\/HypTestTSPRT.html#notifyAll():Unit", "kind" : "final def"}, {"label" : "notify", "tail" : "(): Unit", "member" : "scala.AnyRef.notify", "link" : "edu\/stmc\/HypTestTSPRT.html#notify():Unit", "kind" : "final def"}, {"label" : "toString", "tail" : "(): String", "member" : "scala.AnyRef.toString", "link" : "edu\/stmc\/HypTestTSPRT.html#toString():String", "kind" : "def"}, {"label" : "equals", "tail" : "(arg0: Any): Boolean", "member" : "scala.AnyRef.equals", "link" : "edu\/stmc\/HypTestTSPRT.html#equals(x$1:Any):Boolean", "kind" : "def"}, {"label" : "hashCode", "tail" : "(): Int", "member" : "scala.AnyRef.hashCode", "link" : "edu\/stmc\/HypTestTSPRT.html#hashCode():Int", "kind" : "def"}, {"label" : "getClass", "tail" : "(): Class[_]", "member" : "scala.AnyRef.getClass", "link" : "edu\/stmc\/HypTestTSPRT.html#getClass():Class[_]", "kind" : "final def"}, {"label" : "asInstanceOf", "tail" : "(): T0", "member" : "scala.Any.asInstanceOf", "link" : "edu\/stmc\/HypTestTSPRT.html#asInstanceOf[T0]:T0", "kind" : "final def"}, {"label" : "isInstanceOf", "tail" : "(): Boolean", "member" : "scala.Any.isInstanceOf", "link" : "edu\/stmc\/HypTestTSPRT.html#isInstanceOf[T0]:Boolean", "kind" : "final def"}], "class" : "edu\/stmc\/HypTestTSPRT.html", "kind" : "class"}]};