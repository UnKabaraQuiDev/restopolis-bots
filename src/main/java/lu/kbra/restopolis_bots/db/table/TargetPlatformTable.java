package lu.kbra.restopolis_bots.db.table;

import lu.kbra.pclib.db.table.AbstractDBTable;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.TargetPlatformData;

public interface TargetPlatformTable<T extends TargetPlatformData> extends AbstractDBTable<T> {

	TargetPlatform getTargetPlatform();
	
}
