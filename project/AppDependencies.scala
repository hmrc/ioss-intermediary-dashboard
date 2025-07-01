import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.13.0"
  private val hmrcMongoVersion = "2.6.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "uk.gov.hmrc"             %% "domain-play-30"             % "10.0.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion            % Test,
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.11.0"                  % Test,
    "org.scalacheck"          %% "scalacheck"                 % "1.18.1"                    % Test
  )

  val it: Seq[ModuleID] = Seq.empty
}
