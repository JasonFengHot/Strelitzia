package tv.ismar.library.injectdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import tv.ismar.library.injectdb.annotation.Column;
import tv.ismar.library.injectdb.annotation.Table;

/**
 * Created by LongHai on 17-4-14.
 */

@Table(name = "db_test", id = "_id")
public class AccountDao extends Model {

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    @JsonProperty("account")
    public String username;

    @Column
    public String password;

}
