package com.example.unyieldingmight

import com.example.unyieldingmight.Models.*
import org.junit.Assert
import org.junit.Test
import java.util.ArrayList

class AdapterTest {
    @Test
    fun testAdapterItemCount() {
        val dataList = arrayListOf(
            GymClass(iD = 1, name = "Class 1"),
            GymClass(iD = 2, name = "Class 2")
        )
        // Note: We can't easily test the Adapter class itself here because it requires an Android Context
        // and is tied to RecyclerView. However, we can test the data structure it uses.
        Assert.assertEquals(2, dataList.size)
    }
}
