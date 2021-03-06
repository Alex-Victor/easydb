package com.github.walker.easydb.exception;

/**
 * <p>
 * Locator of database error, it translates the error code of DB2 into unified
 * error type.
 * </p>
 *
 * @author HuQingmiao
 */
public class DB2ErrorLocator extends ErrorLocator {

    public String getErrorMsg(int errorCode) {
        String errMsg = DataAccessException.UNKNOW_ERROR;

        switch (errorCode) {

            case 17002:// 连接不到数据库
                errMsg = DataAccessException.DB_CONNECT_FAILED;
                break;
        }

        return errMsg;
    }
}
