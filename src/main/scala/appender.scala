class Appender(part: String) {

  println("Za konstruktor służą instrukcje w ciele klasy")

  private[this] var _prependCount = 0

  val partlength = part.length

  def appendTo(text: String): String = text + part

  def prependTo(text: String): String = {
    _prependCount += 1
      part + text
  }

  def prependCount = _prependCount

}
