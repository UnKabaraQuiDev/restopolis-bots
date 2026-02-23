package lu.kbra.restopolis_bots.db;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.connector.DataBaseConnectorFactory;
import lu.kbra.pclib.db.utils.DataBaseEntryUtils;
import lu.kbra.restopolis_bots.config.DbConfigData;

@Component
public class DefaultDataBase extends DataBase {

	public DefaultDataBase(DataBaseConnectorFactory connector, DataBaseEntryUtils dataBaseEntryUtils, DbConfigData config) {
		super(connector, config.getName(), dataBaseEntryUtils);
	}

}
