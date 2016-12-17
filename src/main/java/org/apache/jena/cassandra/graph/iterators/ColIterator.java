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
package org.apache.jena.cassandra.graph.iterators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.apache.jena.cassandra.graph.CassandraConnection;
import org.apache.jena.cassandra.graph.ColumnName;
import org.apache.jena.cassandra.graph.TableName;
import org.apache.jena.util.iterator.SingletonIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.Bytes;

/**
 * An iterator over columns in a cassandra table.
 * 
 * This iterator is used when we need to build query keys for a table.  It is used
 *  to generate a single statement that has all the proper where clauses for the columns
 *  based on the order of the columns in the table.
 *
 */
public class ColIterator extends CascadingIterator<String> {
	
	private final CassandraConnection connection;
	private final String keyspace;
	private final TableName tableName;
	private final List<String> colValues; 
	private final ColumnName colName;
	private final int columnNumber;
	

	/**
	 * Constructor.
	 * @param connection The Cassandra connection.
	 * @param keyspace The keyspace to process
	 * @param tableName The tablename we are processing.
	 * @param columnNumber the column number that this iterator covers.
	 * @param colValues the column Values for the query.
	 */
	public ColIterator(CassandraConnection connection, String keyspace, TableName tableName, int columnNumber, List<String> colValues )
	{
		this.connection = connection;
		this.keyspace = keyspace;
		this.tableName = tableName;
		this.columnNumber=columnNumber;
		this.colName = tableName.getPrimaryKeyColumn(columnNumber);
		this.colValues = colValues;

		if (colValues.get(columnNumber) == null)
		{
			setBaseIterator( getColumnIterator( columnNumber, colValues, tableName ) );
		} else {
			setBaseIterator( new SingletonIterator<String>( colValues.get(columnNumber)) );
		}
		boolean end = true;
		for (int i=columnNumber+1;i<4;i++)
		{
			if (colValues.get(i) != null)
			{
				end = false;
			}
		}
		setEndIter( end );
	}
	
	/**
	 * Creates the subIterator over the next column
	 */
	protected Iterator<String> createSubIter()
	{
				ArrayList<String> newValues = new ArrayList<>( colValues );
				newValues.set( columnNumber, thisValue);
				return new ColIterator(connection, keyspace,  tableName, columnNumber+1, newValues);
	}
	
	@Override
	public String next() {
		if (!hasNext())
		{
			throw new NoSuchElementException();	
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append( String.format( "%s=%s", colName, thisValue ));
		if (subIter != null)
		{
			sb.append( " AND " ).append( subIter.next() );
		} else {
			thisValue = null;
		}
		return sb.toString();
	}
	
	/**
	 * Get an iterator over the column values for the specified column.
	 * The iterator returns the hex value of the column.
	 * @param columnNumber The column number in the table to get Info for.
	 * @param colValues The column values that we are looking for.
	 * @param tableName The table name we are looking in.
	 * @return
	 */
	protected Iterator<String> getColumnIterator(int columnNumber, List<String> colValues, TableName tableName)
	{
		ColumnName reqColumn = tableName.getPrimaryKeyColumn(columnNumber);
		
		StringBuilder queryStr = new StringBuilder(String.format("SELECT DISTINCT %s FROM %s.%s WHERE ",
				reqColumn,
				keyspace, tableName));
		if (columnNumber == 0)
		{
			queryStr.append( reqColumn.getScanValue() );
		} else {
			for (int i=0;i<columnNumber;i++)
			{
				if (i>0)
				{
					queryStr.append( " AND ");
				}
				queryStr.append( String.format( "%s=%s", tableName.getPrimaryKeyColumn(i), colValues.get(i)));
			}
		}
		
		return	WrappedIterator.create(connection.getSession().execute(queryStr.toString()).iterator()).mapWith( new Function<Row,String>(){
				@Override
				public String apply(Row arg0) {
					return Bytes.toHexString(arg0.getBytes(0));
				}});
	}
		

}