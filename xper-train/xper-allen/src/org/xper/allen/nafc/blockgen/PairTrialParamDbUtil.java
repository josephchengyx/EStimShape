package org.xper.allen.nafc.blockgen;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xper.Dependency;

import javax.sql.DataSource;

/**
 * Reads and writes trial generation parameters to a (tstamp, xml) table, giving
 * each training session a record of the parameters that produced it.
 *
 * Modelled on NAFCTrialParamDbUtil, with two differences: the table name is a
 * dependency, so the NAFC and passive pair generators share one implementation;
 * and reads return null rather than throwing when the table is still empty.
 */
public class PairTrialParamDbUtil {

    @Dependency
    DataSource dataSource;

    @Dependency
    String tableName;

    public void writeTrialParams(long tstamp, String xml) {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.update("INSERT INTO " + tableName + " (tstamp, xml) VALUES (?, ?)",
                new Object[]{tstamp, xml});
    }

    public String readTrialParams(long tstamp) {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        try {
            return (String) jt.queryForObject(
                    "SELECT xml FROM " + tableName + " WHERE tstamp = ?",
                    new Object[]{tstamp}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * @return the most recently written parameters, or null if none exist yet.
     */
    public String readLatestTrialParams() {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        try {
            return (String) jt.queryForObject(
                    "SELECT xml FROM " + tableName +
                            " WHERE tstamp = (SELECT MAX(tstamp) FROM " + tableName + ")",
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public DataSource getDataSource() { return dataSource; }
    public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
}