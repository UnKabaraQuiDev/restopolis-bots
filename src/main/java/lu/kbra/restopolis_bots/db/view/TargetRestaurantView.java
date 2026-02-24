package lu.kbra.restopolis_bots.db.view;

import java.util.List;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.annotations.view.DB_View;
import lu.kbra.pclib.db.annotations.view.ViewColumn;
import lu.kbra.pclib.db.annotations.view.ViewTable;
import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.query.QueryBuilder;
import lu.kbra.pclib.db.view.DataBaseView;
import lu.kbra.restopolis_bots.db.ro_data.TargetRestaurantROData;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;
import lu.kbra.restopolis_bots.db.table.TargetRestaurantSectionTable;

@DB_View(
		name = "target_restaurant",
		tables = {
				@ViewTable(typeName = RestaurantTable.class, distinct = true, columns = { @ViewColumn(func = "r.*") }, asName = "r"),
				@ViewTable(
						typeName = RestaurantSectionTable.class,
						join = ViewTable.Type.INNER,
						columns = {},
						on = "r.id = rs.restaurant_id",
						asName = "rs"
				),
				@ViewTable(
						typeName = TargetRestaurantSectionTable.class,
						join = ViewTable.Type.INNER,
						columns = { @ViewColumn(name = "target_id") },
						on = "rs.id = trs.restaurant_section_id",
						asName = "trs"
				) }
)
@Component
public class TargetRestaurantView extends DataBaseView<TargetRestaurantROData> {

	public TargetRestaurantView(DataBase dataBase) {
		super(dataBase);
	}

	public List<TargetRestaurantROData> likeName(String value, int maxChoices, long targetId) {
		return super.query(QueryBuilder.<TargetRestaurantROData>select()
				.limit(maxChoices)
				.where(cb -> cb.match("name", "LIKE", "%" + value + "%").match("target_id", "=", targetId))
				.list());
	}

}
