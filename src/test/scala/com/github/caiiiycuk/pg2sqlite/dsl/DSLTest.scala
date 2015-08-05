package com.github.caiiiycuk.pg2sqlite.dsl

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.github.caiiiycuk.pg2sqlite.dsl.DSL._
import com.github.caiiiycuk.pg2sqlite.schema.Column

class DslTest extends FlatSpec with Matchers {

  "DSL" should "drop braces from line" in {
    val TEST_STRING = """
id bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
url text,
ident character varying(20) DEFAULT "substring"(upper(md5((((999999999)::double precision * random()))::text)), 1, 8) NOT NULL,
created_at timestamp without time zone DEFAULT now()
"""

    TEST_STRING.dropBraces should equal("""
id bigint DEFAULT nextval NOT NULL,
url text,
ident character varying DEFAULT "substring" NOT NULL,
created_at timestamp without time zone DEFAULT now
""")
  }

  "DSL" should "take columns parts" in {
    val TEST_STRING = """
insert into some(a, b, c) values ("a", 2, true);
"""

    TEST_STRING.takeBraces should equal(List(
      "a, b, c", """"a", 2, true"""))
  }

  "DSL" should "extract tokens" in {
    val TEST_STRING = """
      insert(strange text) into(some buffer) table
      """

    TEST_STRING.tokens should equal(List("insert", "strange", "text", "into", "some", "buffer", "table"))
  }

  "DSL" should "extract columns with type" in {
    val COLUMNS = """
id bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
url text,
ident character varying(20) DEFAULT "substring"(upper(md5((((999999999)::double precision * random()))::text)), 1, 8) NOT NULL,
created_at timestamp without time zone DEFAULT now()
"""

    COLUMNS.columns should equal(
      List(Column("id", Some("bigint")),
        Column("url", Some("text")),
        Column("ident", Some("character")),
        Column("created_at", Some("timestamp"))))
  }

  "DSL" should "exclude keywords (CONSTRAINTS, etc.) from columns list" in {
    val COLUMNS = """
id integer DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
location geometry,
owner_geoobject_id bigint,
CONSTRAINT enforce_dims_location CHECK ((st_ndims(location) = 2)),
CONSTRAINT enforce_geotype_location CHECK (((geometrytype(location) = 'POLYGON'::text)
  OR (location IS NULL))),
CONSTRAINT enforce_srid_location CHECK ((st_srid(location) = 3395))
"""

    COLUMNS.columns should equal(
      List(Column("id", Some("integer")),
        Column("location", Some("geometry")),
        Column("owner_geoobject_id", Some("bigint"))))
  }

  "DSL" should "get column name from to_tsvector function call" in {
    val COLUMNS = "to_tsvector('libstemmer_serb_lat_no_diacrit'::regconfig, content)"

    COLUMNS.columns should equal(
      List(Column("content", None)))
  }

  "DSL" should "get column name from lower/upper function call" in {
    val COLUMNS = "lower((email)::text),upper((email_up)::text)"

    COLUMNS.columns should equal(
      List(Column("email", None), Column("email_up", None)))
  }

  "DSL" should "split by comma respect braces" in {
    val TEST_STRING = """
id bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
url text,
ident character varying(20) DEFAULT "substring"(upper(md5((((999999999)::double precision * random()))::text)), 1, 8) NOT NULL,
created_at timestamp without time zone DEFAULT now()
""".replaceAll("\n", "")

    val parts = TEST_STRING.commaSplitRespectBraces
    parts.length should equal(4)
    parts(0) should equal("id bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL")
    parts(1) should equal("url text")
    parts(2) should equal("ident character varying(20) DEFAULT \"substring\"(upper(md5((((999999999)::double precision * random()))::text)), 1, 8) NOT NULL")
    parts(3) should equal("created_at timestamp without time zone DEFAULT now()")
  }
}