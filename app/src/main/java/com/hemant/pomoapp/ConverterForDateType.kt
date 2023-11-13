package com.hemant.pomoapp

import androidx.room.TypeConverter
import java.util.Date


class ConverterForDateType {
    @TypeConverter
    fun fromLong(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.time?.toLong()
    }

}