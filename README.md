# JVM bez boilerplate'u

Nie da sie zacząć bez Hello World'a. Stwórzmy plik hello.scala o treści:

```scala
#!/bin/sh
exec scala "$0" "$@"
!#

object Apka extends App {
  println("Hello")
}
```
Teraz wystarczy `chmod +x hello.scala` i można śmigać (`./hello.scala`)

To korzysta z tego, że kompilator scali potrafi od razu uruchomić program.
Dostęp do argumentów jest przez zmienną `args: Array[String]`.
Na wstępie uwaga o nawiasach w scali: `{}` służą do ograniczania
treści klas i funkcji (jak w Javie), `[]` używa się do parametrów typowych
(jak `<>` w Javie), a `()` do operacji na wartościach. Wobec tego pierwszy
element tablicy `t` jest w `t(0)`. `<>` w scali nie jest nawiasem.

Scala kompiluję się do JVM, więc pdostawy języka są podobne.
Inaczej niż w Javie nie wymaga się powiązania między nazwą pakietu
a ścieżką do pliku źródłowego, więc można pisać gdzie kto chce (w tym
np. można definiować wiele klas w jednym pliku). Przykładowa klasa w Scali:

```scala

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
```

Rzeczy godne uwagi:
* Argumenty konstruktora wypisuje się przy nazwie klasy
* Ciała konstruktora nie wydziela się jak w funkcji, tylko pisze instrukcje w ciele.
* Jak ktoś chce mieć więcej konstruktorów, to może, ale to się odradza - lepiej zrobić fabrykę.
* Argumenty konstruktowa można poprzedzić słowami `val` lub `var` - wtedy będą normalnymi atrybutami (domyślnie argumentów konstruktora nie widać z zewnątrz klasy)
* `val` wprowadza stałą
* `var` wprowadza zmienną
* `def` wprowadza metodę; metoda może nie mieć w ogóle listy argumentów i to jest OK.
* Trzeba oznaczać typy argumentów metod
* Zgodnie z podejściem funkcyjnym, wartość ostatniego wyrażenia w bloku będzie zwrócona jako wynik

# Środowisko
Odpalanie kompilatora scali ręcznie jest uciążliwe. Do buildów cały świat używa [sbt](http://www.scala-sbt.org/). Całe sbt konfiguruje się plikiem `build.sbt`,
ale jak go nie ma, poleci by-convention. Domyślnie sbt uzna, że źródła scali są
w katalogu `src/main/scala` - proponuję więc go stworzyć i pisać tam. Dla przykładu
wrzuć tam plik z klasą `Appender` i odpal sbt. Interesujące polecenia w konsoli sbt:
* `compile` - kompiluje projekt, pokazuje błędy
* `run` - odpala (jeśli mamy tylko jeden obiekt rozszerzający `App`, to wiadomo, co odpalić)
* `console` - uruchamia Scala REPL z dołączonymi naszymi klasami
* `test` - uruchamia testy, oczekuje, że będą w `src/test/scala`
Odpal konsolę, żeby zobaczyć, jak można użyć appendera:

```
> console
[info] Starting scala interpreter...
[info]
Welcome to Scala version 2.10.5 (OpenJDK 64-Bit Server VM, Java 1.8.0_45-internal).
Type in expressions to have them evaluated.
Type :help for more information.

scala> val shouter = new Appender("!!!")
Za konstruktor służą instrukcje w ciele klasy
shouter: Appender = Appender@12f637a5

scala> shouter.appendTo("sudo make me a sandwitch")
res0: String = sudo make me a sandwitch!!!

scala> shouter.prependTo("dangerdanger")
res1: String = !!!dangerdanger

scala> shouter.prependTo("!!!")
res2: String = !!!!!!

scala> shouter.prependCount
res3: Int = 2

scala> shouter prependTo "again..."
res4: String = !!!again...
```
Notki:
* w REPLu można definiować zmienne "na płasko", bez otaczającej klasy
* shouter.prependCount formalnie jest metodą, ale dostęp wygląda jak do zmiennej.
    Scala implementuje Uniform Access Principle. Co więcej, jeśli jakaś klasa
    ma taką bezargumentową metodę (`def`), to w podklasie można to przeciążyć
    stałą (`val`) lub zmienną (`var`). Stałych nie można jednak przeciążać zmiennymi
    ani zmiennych stałymi, bo psuje to kontrakt.
* jeśli metoda obiektowa ma dokładnie jeden argument, mozna pisać `x f a` zamiast `x.f(a)`.
   Jeśli wynikiem znów jest obiekt, to mozna chainować w `x f1 a f2 b ...`. Taki styl
   poleca się przy funkcjach wyższych rzędów i raczej odradza wszędzie indziej, ale można
   go fajnie wykorzystać przy budowaniu dsli (można np. zrobić bibliotekę do rozmawiania
   z bazą danych tak, żeby dało się pisać rzeczy w stylu
   `val myQuery = query select name from people where (age > 20)`, co tłumaczy się
   na `query.select(name).from(people).where(age > 20)`, czyli o ile mamy odpowiednie
   rzeczy w scope'ie może się udać).
* dodatkowo: metoda może się nazywać dziwnie, np. `++=`.
  Wtedy wywoływanie bez kropek ma sens - dostajemy swoje operatory za darmo.
  Uwaga: jeśli nazwa operatora kończy się dwukropkiem, metoda zostanie wywołana na
  rzecz *prawego* operandu, czyli `a +: b` znaczy tyle, co `b.+:(a)`.

# Cukierki

## Krotki
```scala
val dwaitrzy = (2,3)
val dwa = dwa._1
val trzy = dwa._2

val (dwa2, trzy2) = dwaitrzy
```
* przydają się to obliczeń na szybko - można zrobić mapę krotek etc.

## Traity

```scala
trait Shouter {
  def shout: String
  def shoutTwice = shout + shout
}

trait BoringShouter extends Shouter {
  override val shout: String
}
```
* `trait` jest jak interfejs, ale może mieć ciała metod i zmienne - dzięki temu można robić bogate interfejsy
* wszystko, co się przeciąża musi być oznaczone `override`
* można dziedziczyć po wielu traitach

## Any, AnyVal, AnyRef, Nothing, Unit, None, ???
To po kolei:
* `Any` to korzeń hierarchii typów. Każda wartość jest typu `Any`.
* `AnyRef` to korzeń obiektowej hierarchii. Każdy obiekt jest typu `AnyRef`.
* `AnyVal` to marker mówiący, że dany typ jest reprezentowany "na płasko", a nie przez referencję. To zaawansowany temat, nie będzie nam potrzebne (google "scala value types")
* `Nothing` to dolny korzeń hierarchii typów. `Nothing` jest podklasą każdego typu (nawet takiego, którego jeszcze nie ma, a dopiero go stworzysz),
   ale nie da się wyprodukować żadnej wartości typu `Nothing`. Jeśli stworzysz z czapy pustą listę, będzie ona typu `List[Nothing]`.
* `Unit` to typ, który ma tylko jedną wartość. Ta wartość nazywa się `()`. Używa się go do oznaczenia efektów ubocznych, np. funkcja, która zapisuje coś
   do bazy mogłaby mieć sygnaturę `def save(record: DBRecord): Unit`.
* `None` to jeden z dwóch konstruktorów typu `Option`, o którym jeszcze będzie
* `???` to wyrażenie typu `Nothing`, które można wstawić w dowolne miejsce. Próba jego wyliczenia podniesie `NotImplementedError`.

## Obiekty

```scala
object BoringBlaher extends BoringShouter {
  override val shout = "blah"
}
```
*Obiekty są super.* Obiekt zadeklarowany słowem kluczowym `object` jest jeden unikalny
dla okalającego scope'u. To znaczy, że jeśli zadeklarujemy `object` w jakimś pakiecie,
de facto dostaniemy singleton. Jest to jednocześnie typu oraz jedynej dostępnej wartości tego typu.
W Scali nie ma metod statycznych - obiekty wypełniają tę dziurę dużo lepiej,
np. bo mogą implementować różne traity.

## Companion objects
W pliku, w którym definiujemy klasę/trait `T`, można też zadefiniować obiekt `T`.
Wówczas ten obiekt jest `companionem` klasy i służy za uniwersalną fasadę/fabrykę/wszystko, co statyczne.
Można w nim zadeklarowac np. różne typy i metody fabryczne:
```scala
trait UsosWersjaSiedemZero {
  def dodajStudenta(papiery: PapieryDoRejestracji): Student
  def wywalStudenta(student: Student): Unit
}

object UsosWersjaSiedemZero {
  type PapieryDoRejestracji = (ZgłoszenieTakie, ZgłoszenieSiakie, Zdjęcie)
  case class Student(imie: String, nazwisko: String, index: String) { ??? }
}
```

## Funkcje wyższych rzędów

```scala
List(1,2,3,4,5).map(t => t+1).map(_+1).filter(_ % 2 == 0).count(_%3 == 0)
```

## Option
Funkcyjne opakowanie na "może wartość":

```scala
def twice(x: Int) = 2*x

def maybeTwice(x: Option[Int]): Option[Int] = x.map(twice)

def twiceOrZero(x: Option[Int]): Int = x.map(twice).getOrElse(0)

maybeTwice(Some(5))   // Some(10)
maybeTwice(None)      // None
twiceOrZero(Some(5))  // 10
twiceOrZero(None)     // 0
```
*Uwaga na nulle:* w Scali nie używa się nulli - jeśli czegoś może nie być,
oddaje się `Option`. Niestety JVM jest tak skonstruowany, że zawsze ktoś może
przekazać `null` w miejsce parametru typu obiektowego i może wylecieć `NullPointerException`. Smutek.

## Typy algebraiczne
```scala
sealed trait Albo[+A, +B]

case class Fajne[+A, +B](a: A) extends Albo[A, B]
case class Kiepskie[+A, +B](b: B) extends Albo[A, B]


def parzyste(x: Int) = if (x%2 == 0) Fajne(x) else Kiepskie(x)

def czyFajne[A, B](x: Albo[A, B]) = x match {
  case Fajne(f) => {
    println("Fajne: " + f.toString)
    true
  }
  case Kiepskie(_) => false
}
```
* `sealed` znaczy, że dany trait (klasa) może być rozszerzany tylko w tym pliku źródłowym
* Plusy w `Fajne[+A, +B]` mówią, że `Fajne` jest _kowariantne_ na parametrach typowych,
   czyli po chłopsku _jeśli A1 jest podtypem A, to Fajne[A1, B] jest podtypem Fajne[A, B]_.
   Akurat dla typu `Fajne` to się zgadza, ale np. tablice są już _inwariantne_,
   a obiekty zjadające rzeczy są _kontrawariantne_.
* `case class` różni się od zwykłej klasy kilkoma szczegółami:
  * parametry konstruktora są widoczne na zewnątrz
  * działa pattern matching
  * dostajemy za darmo serializację i toString.
* *Pattern matching jest super*.
* Btw. takie `Albo` już jest w Scali, nazywa się `Either`, i ma całą masę przydatnych metod
  (np. `Left(4).right.map(_+2).left.map(_+1)` ma wartość `5`).

## Implicit parameters
To jest ficzer, którym można sobie zrobić krzywdę. Ostrzegłem.

Czasem chcemy do funkcji przekazać jakiś kontekst. Wyobraźmy sobie na przykład,
że mamy klasę reprezentującą kiedyś możliwe że wynik obliczenia i chcemy móc
powiedzieć 'jak to obliczenie już się kończy, to przekształć wynik dalej'; co więcej,
chcemy to robić w jakiejś puli workerów.
Wówczas listę parametrów funkcji można oznaczyć jako `implicit`:
```scala

trait PewnegoDnia[A] {
  def czekaj: A  // blokuje, aż obliczenie się zakończy i zwraca wynik
  def aPotem[B](f: A => B)(implicit robol: PulaRoboli) =
    robol.noweZadanie(...)
}
```
Takiej funkcji mozna używać jak zwykłej:
```scala
val robole: PulaRoboli = ...
val p: PewnegoDnia[Int] = ...
val p2 = p.aPotem(t => 2*t + 4)(robole)
```

Ale można nie chcieć przekazywać puli roboli wszędzie, bo to zaśmieca kod. Wówczas
można oznaczyć sobię pulę jako `implicit` i dać kompilatorowi się domyslić:
```scala
implicit val robole: PulaRoboli = ...
val p: PewnegoDnia[Int] = ...
val p2 = p.aPotem(t => t*2+3)
```
Semantyka jest taka: jeśli jakieś parametry są oznaczone jako `implicit`, to kompilator
będzie szukał w bieżącym scope'ie wartości, których typ pasuje i też są `implicit`.
Jeśli najbardziej pasująca typem wartość będzie dokładnie jedna, to zgadnie i wstawi.

# Co dalej?
* rich interfaces, self-typed traits
* implicit classes, value types
* go reactive:
  * asynchronous programming with Futures
  * streams
  * actors
* parsing (e.g. JSON) with combinators
* Specification checking with specs2
* Java libraries out-of-the-box
* Heavy functional programming: scalaz
