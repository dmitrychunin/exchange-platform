case class OrderUpdateInput(e: String, E: BigInt, s: String, U: BigInt, u: BigInt, b: Seq[Seq[String]], a: Seq[Seq[String]])
//todo  ограничить размер вложенного массива двумя элементами, подключив: "com.chuusai" %% "shapeless" % "2.3.3"

//case class OrderUpdateOutput(e: String, E: BigInt, s: String, U: BigInt, u: BigInt, b: Seq[(String, String)], a: Seq[(String, String)])

case class OrderUpdateOutput(e: String, E: BigInt, s: String, U: BigInt, u: BigInt, b: String, a: String)