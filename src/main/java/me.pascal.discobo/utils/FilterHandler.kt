package me.pascal.discobo.utils

class FilterHandler(val database: DatabaseHandler) {

    val filterList = arrayListOf<String>()

    fun handleFilter(filter: String): Boolean {
        val query = "SELECT * FROM filters WHERE censor=?"
        val temp: Boolean
        val set = database.connection.prepareStatement(query).apply {
            this.setString(1, filter)
        }.executeQuery()

        if (set.next()) {
            //filter already exists
            val query = "DELETE FROM filters WHERE censor=?"
            database.connection.prepareStatement(query).use {
                it.setString(1, filter)
                it.executeUpdate()
            }
            temp = false
        } else {
            //filter not existing
            val query = "INSERT INTO filters (censor) VALUES(?);"
            database.connection.prepareStatement(query).use {
                it.setString(1, filter)
                it.executeUpdate()
            }
            temp = true
        }
        cacheFilters()
        return temp
    }

    fun cacheFilters() {
        filterList.clear()
        val query = "SELECT * FROM filters"
        val set = database.connection.createStatement().executeQuery(query)
        while (set.next()) {
            filterList.add(set.getString(1))
        }
    }

}