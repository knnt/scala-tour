trait Shouter {
  def shout: String
  def shoutTwice = shout + shout
}

trait BoringShouter extends Shouter {
  override val shout: String
}
