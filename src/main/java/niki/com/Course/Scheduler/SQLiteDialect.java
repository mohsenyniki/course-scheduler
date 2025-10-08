package niki.com.Course.Scheduler;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.StandardSqlAstTranslatorFactory;

/**
 * Minimal SQLite dialect compatible with Hibernate 6+. This class keeps method
 * signatures simple and avoids use of the removed registerColumnType API.
 * It intentionally omits @Override annotations on methods that may vary between
 * Hibernate versions to maximize compatibility.
 */
public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
    }

    public IdentityColumnSupport getIdentityColumnSupport() {
        return new IdentityColumnSupportImpl();
    }

    /**
     * Provide the standard SqlAstTranslatorFactory so Hibernate 6 can build
     * SQL AST translators for mutation operations (required for collections).
     */
    public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
        return new StandardSqlAstTranslatorFactory();
    }

    public boolean supportsLimit() {
        return true;
    }

    public String getLimitString(String query, boolean hasOffset) {
        return query + (hasOffset ? " limit ? offset ?" : " limit ?");
    }

    public boolean supportsTemporaryTables() {
        return true;
    }

    public String getCreateTemporaryTableString() {
        return "create temporary table if not exists";
    }

    public String getCurrentTimestampSelectString() {
        return "select current_timestamp";
    }

}
