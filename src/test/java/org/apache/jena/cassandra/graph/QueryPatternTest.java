/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.cassandra.graph;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.thrift.TException;
import org.junit.Test;

public class QueryPatternTest {

	private static String graphHexValue = "0x0c00010b000100000018687474703a2f2f65786d61706c652e636f6d2f67726170680000";
	private static String graphHex = " graph=" + graphHexValue + " ";

	private static String subjectHexValue = "0x0c00010b00010000001a687474703a2f2f65786d61706c652e636f6d2f7375626a6563740000";
	private static String subjectHex = " subject=" + subjectHexValue + " ";

	private static String predicateHexValue = "0x0c00010b00010000001c687474703a2f2f65786d61706c652e636f6d2f7072656469636174650000";
	private static String predicateHex = " predicate=" + predicateHexValue + " ";

	private static String objectHexValue = "0x0c00010b000100000019687474703a2f2f65786d61706c652e636f6d2f6f626a6563740000";
	private static String objectHex = " object=" + objectHexValue + " ";

	private static Node graph = NodeFactory.createURI("http://exmaple.com/graph");
	private static Node subject = NodeFactory.createURI("http://exmaple.com/subject");
	private static Node predicate = NodeFactory.createURI("http://exmaple.com/predicate");
	private static Node object = NodeFactory.createURI("http://exmaple.com/object");

	private static Node node42 = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(42));
	private static String node42HexValue = "0x3432";

	private static Node nodeLit = NodeFactory.createLiteral("String Literal");
	private static String nodeLitHexValue = "0x537472696e67204c69746572616c";
	private static String nodeLitDType = "'http://www.w3.org/2001/XMLSchema#string'";

	private static Node nodeLitLang = NodeFactory.createLiteral("String Literal", "en-US");
	private static String nodeLitLangDType = "'http://www.w3.org/1999/02/22-rdf-syntax-ns#langString'";

	private final static String SELECT_COLUMNS;

	static {
		ColumnName[] cols = new ColumnName[6];
		for (ColumnName c : ColumnName.values()) {
			if (c.getQueryPos() != -1) {
				cols[c.getQueryPos()] = c;
			}

		}
		StringBuilder sb = new StringBuilder();
		for (ColumnName c : cols) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(c);

		}
		SELECT_COLUMNS = sb.toString();
	}

	@Test
	public void fullFindQueryTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, object);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		// add extra space to match internal tests
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

	}

	@Test
	public void fullFindQuerySuffixTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, object);
		QueryPattern qp = new QueryPattern(q);

		// add extra space to match internal tests
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		qi.suffix="limit 1";
		String s = qp.getFindQuery("test", qi);
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));
		assertTrue("suffix missing", s.contains(" limit 1"));
	}

	@Test
	public void fullFindQueryExtraTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, object);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		qi.extraWhere="something=Something";
		// add extra space to match internal tests
		String s = qp.getFindQuery("test", qi);
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));
		assertTrue("extra missing", s.contains(" AND something=Something"));
	}

	@Test
	public void fullFindQueryExtraSuffixTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, object);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		qi.extraWhere="something=Something";
		qi.suffix="limit 1";
		// add extra space to match internal tests
		String s = qp.getFindQuery("test", qi);
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));
		assertTrue("extra missing", s.contains(" AND something=Something "));
		assertTrue("suffix missing", s.contains(" limit 1"));
	}

	@Test
	public void fullLiteralFindQueryTest() throws TException {

		Quad q = new Quad(graph, subject, predicate, node42);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " found", s.contains(ColumnName.I + "=42"));
	}

	@Test
	public void singleAnyLiteralFindQueryTest() throws TException {

		Quad q = new Quad(graph, subject, Node.ANY, node42);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " found", s.contains(ColumnName.I + "=42"));

		q = new Quad(graph, Node.ANY, predicate, node42);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		s = qp.getFindQuery("test", qi) + " ";
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("table missing", s.contains("test.POGS"));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " found", s.contains(ColumnName.I + "=42"));

		q = new Quad(Node.ANY, subject, predicate, node42);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " found", s.contains(ColumnName.I + "=42"));

	}

	@Test
	public void doubleAnyLiteralFindQueryTest() throws TException {

		Quad q = new Quad(graph, Node.ANY, Node.ANY, node42);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " found", s.contains(ColumnName.I + "=42"));

		q = new Quad(Node.ANY, subject, Node.ANY, node42);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " found", s.contains(ColumnName.I + "=42"));

		q = new Quad(Node.ANY, Node.ANY, predicate, node42);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.POGS"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " found", s.contains(ColumnName.I + "=42"));
	}

	@Test
	public void tripleAnyLiteralFindQueryTest() throws TException {

		Quad q = new Quad(Node.ANY, Node.ANY, Node.ANY, node42);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertTrue(ColumnName.I + " missing", s.contains(ColumnName.I + "=42"));
		assertTrue("graph scan missing", s.contains(" token(graph) >= -9223372036854775808 "));

	}

	@Test
	public void singleAnyFindQueryTest() throws TException {

		Quad q = new Quad(graph, subject, predicate, Node.ANY);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(graph, subject, Node.ANY, object);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.OSGP"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(graph, Node.ANY, predicate, object);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.POGS"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(Node.ANY, subject, predicate, object);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

	}

	@Test
	public void doubleAnyFindQueryTest() throws TException {

		Quad q = new Quad(graph, subject, Node.ANY, Node.ANY);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("graph missing", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		/* this one need a filter */
		q = new Quad(graph, Node.ANY, predicate, Node.ANY);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.POGS"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(Node.ANY, subject, predicate, Node.ANY);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		/*
		 * this one requires a filter
		 */
		q = new Quad(graph, Node.ANY, Node.ANY, object);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.OSGP"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(Node.ANY, subject, Node.ANY, object);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.OSGP"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(Node.ANY, Node.ANY, predicate, object);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.POGS"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

	}

	@Test
	public void tripleAnyFindQueryTest() throws TException {

		Quad q = new Quad(Node.ANY, Node.ANY, Node.ANY, object);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.OSGP"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertTrue("object missing", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(Node.ANY, Node.ANY, predicate, Node.ANY);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.POGS"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertTrue("predicate missing", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(Node.ANY, subject, Node.ANY, Node.ANY);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.SPOG"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertTrue("subject missing", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

		q = new Quad(graph, Node.ANY, Node.ANY, Node.ANY);
		qp = new QueryPattern(q);
		qi = qp.new QueryInfo(q);
		 s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertTrue("graph missing", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));

	}

	@Test
	public void quadAnyFindQueryTest() throws TException {

		Quad q = new Quad(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
		QueryPattern qp = new QueryPattern(q);
		QueryPattern.QueryInfo qi = qp.new QueryInfo(q);
		String s = qp.getFindQuery("test", qi) + " ";
		assertTrue("table missing", s.contains("test.GSPO"));
		assertTrue("Standard query columns missing", s.contains("SELECT " + SELECT_COLUMNS + " FROM "));
		assertFalse("graph found", s.contains(graphHex));
		assertFalse("subject found", s.contains(subjectHex));
		assertFalse("predicate found", s.contains(predicateHex));
		assertFalse("object found", s.contains(objectHex));
		assertFalse(ColumnName.I + " found", s.contains(ColumnName.I.toString()));
		assertTrue("graph scan missing", s.contains(" token(graph) >= -9223372036854775808 "));

	}
	
	@Test
	public void numericInsertTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, node42);
		QueryPattern qp = new QueryPattern(q);
		String s = qp.getInsertStatement("test");
		String[] lines = s.split("\n");
		assertEquals(6, lines.length);
		assertEquals("BEGIN BATCH", lines[0]);

		assertTrue("table missing", lines[1].contains("test.GSPO"));
		assertTrue("insert columns missing: "+lines[1],
				lines[1].contains("(subject, predicate, object, graph, " + ColumnName.D + ", " + ColumnName.I + ")"));
		assertTrue("graphHexValue missing", lines[1].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[1].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[1].contains(" " + predicateHexValue + ", "));
		assertTrue("node42HexValue missing", lines[1].contains(" " + node42HexValue + ", "));
		assertTrue("data type missing", lines[1].contains(", 'http://www.w3.org/2001/XMLSchema#int', "));
		assertTrue("42 missing", lines[1].contains("42);"));

		assertTrue("table missing", lines[2].contains("test.OSGP"));
		assertTrue("insert columns missing",
				lines[2].contains("(subject, predicate, object, graph, " + ColumnName.D + ", " + ColumnName.I + ")"));
		assertTrue("graphHexValue missing", lines[2].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[2].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[2].contains(" " + predicateHexValue + ", "));
		assertTrue("node42HexValue missing", lines[2].contains(" " + node42HexValue + ", "));
		assertTrue("data type missing", lines[2].contains(", 'http://www.w3.org/2001/XMLSchema#int', "));
		assertTrue("42 missing", lines[2].contains("42);"));

		assertTrue("table missing", lines[3].contains("test.POGS"));
		assertTrue("insert columns missing",
				lines[3].contains("(subject, predicate, object, graph, " + ColumnName.D + ", " + ColumnName.I + ")"));
		assertTrue("graphHexValue missing", lines[3].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[3].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[3].contains(" " + predicateHexValue + ", "));
		assertTrue("node42HexValue missing", lines[3].contains(" " + node42HexValue + ", "));
		assertTrue("data type missing", lines[3].contains(", 'http://www.w3.org/2001/XMLSchema#int', "));
		assertTrue("42 missing", lines[3].contains("42);"));

		assertTrue("table missing", lines[4].contains("test.SPOG"));
		assertTrue("insert columns missing",
				lines[4].contains("(subject, predicate, object, graph, " + ColumnName.D + ", " + ColumnName.I + ")"));
		assertTrue("graphHexValue missing", lines[4].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[4].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[4].contains(" " + predicateHexValue + ", "));
		assertTrue("node42HexValue missing", lines[4].contains(" " + node42HexValue + ", "));
		assertTrue("data type missing", lines[4].contains(", 'http://www.w3.org/2001/XMLSchema#int', "));
		assertTrue("42 missing", lines[4].contains("42);"));

		assertEquals("APPLY BATCH;", lines[5]);

	}

	@Test
	public void literalInsertTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, nodeLit);
		QueryPattern qp = new QueryPattern(q);
		String s = qp.getInsertStatement("test");
		String[] lines = s.split("\n");
		assertEquals(6, lines.length);
		assertEquals("BEGIN BATCH", lines[0]);

		assertTrue("table missing", lines[1].contains("test.GSPO"));
		assertTrue("insert columns missing: "+lines[1],
				lines[1].contains("(subject, predicate, object, graph, " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[1].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[1].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[1].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[1].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[1].contains(", " + nodeLitDType + ")"));

		assertTrue("table missing", lines[2].contains("test.OSGP"));
		assertTrue("insert columns missing: "+lines[2],
				lines[2].contains("(subject, predicate, object, graph, " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[2].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[2].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[2].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[2].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[2].contains(", " + nodeLitDType + ")"));

		assertTrue("table missing", lines[3].contains("test.POGS"));
		assertTrue("insert columns missing: "+lines[3],
				lines[3].contains("(subject, predicate, object, graph, " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[3].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[3].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[3].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[3].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[3].contains(", " + nodeLitDType + ")"));

		assertTrue("table missing", lines[4].contains("test.SPOG"));
		assertTrue("insert columns missing: "+lines[4],
				lines[4].contains("(subject, predicate, object, graph, " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[4].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[4].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[4].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[4].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[4].contains(", " + nodeLitDType + ")"));

		assertEquals("APPLY BATCH;", lines[5]);

	}

	@Test
	public void literalWithLangInsertTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, nodeLitLang);
		QueryPattern qp = new QueryPattern(q);
		String s = qp.getInsertStatement("test");
		String[] lines = s.split("\n");
		assertEquals(6, lines.length);
		assertEquals("BEGIN BATCH", lines[0]);

		assertTrue("table missing", lines[1].contains("test.GSPO"));
		assertTrue("insert columns missing:"+lines[1],
				lines[1].contains("(subject, predicate, object, graph, " + ColumnName.L + ", " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[1].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[1].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[1].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[1].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[1].contains(" " + nodeLitLangDType + ")"));
		assertTrue("lang missing", lines[1].contains(", 'en-us', "));

		assertTrue("table missing", lines[2].contains("test.OSGP"));
		assertTrue("insert columns missing:"+lines[2],
				lines[2].contains("(subject, predicate, object, graph, " + ColumnName.L + ", " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[2].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[2].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[2].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[2].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[2].contains(" " + nodeLitLangDType + ")"));
		assertTrue("lang missing", lines[2].contains(", 'en-us', "));

		assertTrue("table missing", lines[3].contains("test.POGS"));
		assertTrue("insert columns missing:"+lines[3],
				lines[3].contains("(subject, predicate, object, graph, " + ColumnName.L + ", " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[3].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[3].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[3].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[3].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[3].contains(" " + nodeLitLangDType + ")"));
		assertTrue("lang missing", lines[3].contains(", 'en-us', "));

		assertTrue("table missing", lines[4].contains("test.SPOG"));
		assertTrue("insert columns missing:"+lines[4],
				lines[4].contains("(subject, predicate, object, graph, " + ColumnName.L + ", " + ColumnName.D + ")"));
		assertTrue("graphHexValue missing", lines[4].contains(" " + graphHexValue + ", "));
		assertTrue("subjectHexValue missing", lines[4].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[4].contains(" " + predicateHexValue + ", "));
		assertTrue("nodeLitHexValue missing", lines[4].contains(" " + nodeLitHexValue + ", "));
		assertTrue("data type missing", lines[4].contains(" " + nodeLitLangDType + ")"));
		assertTrue("lang missing", lines[4].contains(", 'en-us', "));

		assertEquals("APPLY BATCH;", lines[5]);

	}

	@Test
	public void insertTest() throws TException {
		Quad q = new Quad(graph, subject, predicate, object);
		QueryPattern qp = new QueryPattern(q);
		String s = qp.getInsertStatement("test");
		String[] lines = s.split("\n");
		assertEquals(6, lines.length);
		assertEquals("BEGIN BATCH", lines[0]);

		assertTrue("table missing", lines[1].contains("test.GSPO"));
		assertTrue("insert columns missing", lines[1].contains("(subject, predicate, object, graph)"));
		assertTrue("graphHexValue missing", lines[1].contains(" " + graphHexValue + ");"));
		assertTrue("subjectHexValue missing", lines[1].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[1].contains(" " + predicateHexValue + ", "));
		assertTrue("objectHexValue missing", lines[1].contains(" " + objectHexValue + ", "));
		assertFalse("42 found", lines[1].contains("42);"));

		assertTrue("table missing", lines[2].contains("test.OSGP"));
		assertTrue("insert columns missing", lines[2].contains("(subject, predicate, object, graph)"));
		assertTrue("graphHexValue missing", lines[2].contains(" " + graphHexValue + ");"));
		assertTrue("subjectHexValue missing", lines[2].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[2].contains(" " + predicateHexValue + ", "));
		assertTrue("objectHexValue missing", lines[2].contains(" " + objectHexValue + ", "));
		assertFalse("42 found", lines[2].contains("42);"));

		assertTrue("table missing", lines[3].contains("test.POGS"));
		assertTrue("insert columns missing", lines[3].contains("(subject, predicate, object, graph)"));
		assertTrue("graphHexValue missing", lines[3].contains(" " + graphHexValue + ");"));
		assertTrue("subjectHexValue missing", lines[3].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[3].contains(" " + predicateHexValue + ", "));
		assertTrue("objectHexValue missing", lines[3].contains(" " + objectHexValue + ", "));
		assertFalse("42 found", lines[3].contains("42);"));

		assertTrue("table missing", lines[4].contains("test.SPOG"));
		assertTrue("insert columns missing", lines[4].contains("(subject, predicate, object, graph)"));
		assertTrue("graphHexValue missing", lines[4].contains(" " + graphHexValue + ");"));
		assertTrue("subjectHexValue missing", lines[4].contains(" " + subjectHexValue + ", "));
		assertTrue("predicateHexValue missing", lines[4].contains(" " + predicateHexValue + ", "));
		assertTrue("objectHexValue missing", lines[4].contains(" " + objectHexValue + ", "));
		assertFalse("42 found", lines[4].contains("42);"));

		assertEquals("APPLY BATCH;", lines[5]);

	}
}
