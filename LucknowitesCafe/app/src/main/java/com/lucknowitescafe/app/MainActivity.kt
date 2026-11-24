package com.lucknowitescafe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Food(val category:String,val name:String,val prices:List<Int>)
data class Line(val food:Food,val price:Int,val qty:Int)
data class CafeOrder(val number:Int,val customer:String,val phone:String,val fulfillment:String,val address:String,val lines:List<Line>,val total:Int)
val menu=listOf(
Food("Budget Combo","Chai + Bun Makkhan",listOf(49)),Food("Budget Combo","Chai + Poha",listOf(49)),Food("Budget Combo","Fries + Chai",listOf(59)),Food("Budget Combo","Fries / Potato Bites + Chai + Coffee",listOf(79)),Food("Budget Combo","Chai & Paratha",listOf(59)),Food("Budget Combo","Chai & Chilla",listOf(59)),Food("Budget Combo","Chilla & Coffee",listOf(69)),Food("Budget Combo","Special Combo: Potato Bites, Fries & Chai/Coffee",listOf(120)),
Food("Breakfast","Masala Chai",listOf(10,15,20)),Food("Breakfast","Tandoori Chai",listOf(40)),Food("Breakfast","Coffee",listOf(40)),Food("Breakfast","Cold Coffee",listOf(80)),
Food("Snacks","Masala Maggie",listOf(50)),Food("Snacks","Butter Maggie",listOf(60)),Food("Snacks","Bread Pakoda",listOf(20)),Food("Snacks","Paneer Pakoda",listOf(30)),Food("Snacks","Samosa",listOf(20)),Food("Snacks","Bun Makkhan",listOf(30)),Food("Snacks","Pyaaz Pakoda",listOf(30)),Food("Snacks","French Fries",listOf(40)),Food("Snacks","Potato Bites",listOf(40)),Food("Snacks","Idli",listOf(30,60)),Food("Snacks","Burger",listOf(40)),Food("Snacks","Sandwich",listOf(50)),
Food("Lunch","Biryani",listOf(70,150)),Food("Lunch","Aloo Paratha",listOf(40)),Food("Lunch","Chhole Chawal",listOf(50)),Food("Lunch","Puri Sabji",listOf(50)),
Food("Health Conscious","Sprouts",listOf(20,30)),Food("Health Conscious","Poha",listOf(40,80)),Food("Health Conscious","Sweet Corn Mix Chilla",listOf(40)),Food("Health Conscious","Ragi Kachori",listOf(50)),Food("Evening Cravings","Veg Momos",listOf(40)),Food("Evening Cravings","Paneer Momos",listOf(50)))
val ink=Color(0xFF28140D); val gold=Color(0xFFFFC247); val cream=Color(0xFFFFF4DF)
val tea=Color(0xFF7A351D); val orange=Color(0xFFE87524); val leaf=Color(0xFF477A43); val cardCream=Color(0xFFFFE8C2)

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{CafeApp()}}}

@Composable fun CafeApp(){
 var admin by remember{mutableStateOf(false)}
 var cart by remember{mutableStateOf(listOf<Line>())}
 var orders by remember{mutableStateOf(listOf<CafeOrder>())}
 var pin by remember{mutableStateOf("")}
 var login by remember{mutableStateOf(false)}
 var msg by remember{mutableStateOf("")}
 MaterialTheme(colorScheme=lightColorScheme(primary=tea,secondary=gold,background=cream,surface=Color.White)){
  Column(Modifier.fillMaxSize().background(cream)){
   Column(Modifier.fillMaxWidth().background(ink).padding(horizontal=18.dp, vertical=16.dp)) {
    Row(verticalAlignment=Alignment.CenterVertically) {
      Surface(color=gold,shape=RoundedCornerShape(50),modifier=Modifier.size(54.dp)) {
        Box(contentAlignment=Alignment.Center){Text("☕",fontSize=30.sp)}
      }
      Spacer(Modifier.width(12.dp))
      Column {
        Text("LUCKNOWITES",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.ExtraBold)
        Text("CAFE",color=gold,fontSize=22.sp,fontWeight=FontWeight.Bold)
        Text("Every Meal is a Celebration",color=Color(0xFFFFE8C2),fontSize=12.sp)
      }
    }
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      Text("CHAI",color=gold,fontWeight=FontWeight.Bold,fontSize=11.sp)
      Text("• SNACKS • COMBOS • MEALS",color=Color.White,fontSize=11.sp)
    }
  }
   Row(Modifier.fillMaxWidth().padding(8.dp)){Button(onClick={admin=false},colors=ButtonDefaults.buttonColors(containerColor=tea),modifier=Modifier.weight(1f)){Text("Customer")};Spacer(Modifier.width(8.dp));Button(onClick={login=true},colors=ButtonDefaults.buttonColors(containerColor=orange),modifier=Modifier.weight(1f)){Text("Admin")}}
   if(admin) AdminPage(orders) else CustomerPage(cart,
    {f,p->val i=cart.indexOfFirst{it.food.name==f.name&&it.price==p};cart=if(i<0)cart+Line(f,p,1) else cart.mapIndexed{n,l->if(n==i)l.copy(qty=l.qty+1)else l};msg="Added to cart"},
    {l->cart=cart.mapNotNull{if(it==l){if(it.qty>1)it.copy(qty=it.qty-1)else null}else it}},
    {name,phone,mode,address->
      val total=cart.sumOf{it.price*it.qty}
      orders=orders+CafeOrder(orders.size+1,name,phone,mode,address,cart,total)
      cart=emptyList();msg="Order placed. Order #${orders.size} saved on this device."
    })
   if(msg.isNotBlank())Text(msg,Modifier.padding(8.dp),color=ink)
  }
  if(login)AlertDialog(onDismissRequest={login=false},title={Text("Admin Login")},text={Column{Text("Demo PIN: 1234");OutlinedTextField(pin,{pin=it},label={Text("PIN")})}},confirmButton={TextButton(onClick={if(pin=="1234"){admin=true;login=false;pin=""}else msg="Incorrect PIN"}){Text("Login")}},dismissButton={TextButton(onClick={login=false}){Text("Cancel")}})
 }
}

@Composable fun CustomerPage(cart:List<Line>,add:(Food,Int)->Unit,remove:(Line)->Unit,place:(String,String,String,String)->Unit){
 val cats=menu.map{it.category}.distinct()
 var cat by remember{mutableStateOf(cats.first())}
 var checkout by remember{mutableStateOf(false)}
 var customer by remember{mutableStateOf("")}
 var phone by remember{mutableStateOf("")}
 var address by remember{mutableStateOf("")}
 var mode by remember{mutableStateOf("Pickup from Cafe")}
 Column(Modifier.fillMaxSize()){
  ScrollableTabRow(selectedTabIndex=cats.indexOf(cat)){cats.forEach{Tab(cat==it,{cat=it},text={Text(it)})}}
  LazyColumn(Modifier.weight(1f).padding(horizontal=10.dp)){items(menu.filter{it.category==cat}){f->
   Card(Modifier.fillMaxWidth().padding(vertical=5.dp),colors=CardDefaults.cardColors(containerColor=cardCream),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Text(f.name,fontWeight=FontWeight.Bold,color=ink,fontSize=17.sp);Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){f.prices.forEachIndexed{i,p->Button(onClick={add(f,p)},colors=ButtonDefaults.buttonColors(containerColor=leaf)){Text(if(f.prices.size==1)"Add ₹$p" else "Option ${i+1} ₹$p")}}}}}
  }}
  val total=cart.sumOf{it.price*it.qty}
  Card(Modifier.fillMaxWidth().padding(8.dp)){Column(Modifier.padding(10.dp)){Text("Cart • ₹$total",fontWeight=FontWeight.Bold,fontSize=18.sp);cart.forEach{l->Row(verticalAlignment=Alignment.CenterVertically){Text("${l.food.name} × ${l.qty}  ₹${l.price*l.qty}",Modifier.weight(1f),fontSize=12.sp);TextButton(onClick={remove(l)}){Text("−")}}};Button(onClick={checkout=true},enabled=cart.isNotEmpty(),colors=ButtonDefaults.buttonColors(containerColor=orange),modifier=Modifier.fillMaxWidth()){Text("Continue to Checkout")}}}
 }
 if(checkout) AlertDialog(onDismissRequest={checkout=false},title={Text("Customer & Delivery Details")},text={
  Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
   OutlinedTextField(customer,{customer=it},label={Text("Customer name")},singleLine=true)
   OutlinedTextField(phone,{phone=it},label={Text("Mobile number")},singleLine=true)
   Text("Order type")
   Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=mode=="Pickup from Cafe",onClick={mode="Pickup from Cafe"});Text("Pickup");RadioButton(selected=mode=="Home Delivery",onClick={mode="Home Delivery"});Text("Delivery")}
   if(mode=="Home Delivery")OutlinedTextField(address,{address=it},label={Text("Full delivery address")},minLines=2)
   Text("Total: ₹${cart.sumOf{it.price*it.qty}}")
  }
 },confirmButton={TextButton(onClick={if(customer.isNotBlank()&&phone.length>=10&&(mode=="Pickup from Cafe"||address.isNotBlank())){place(customer.trim(),phone.trim(),mode,address.trim());checkout=false}else{}}){Text("Place Order")}},dismissButton={TextButton(onClick={checkout=false}){Text("Cancel")}})
}

@Composable fun AdminPage(orders:List<CafeOrder>){
 Column(Modifier.fillMaxSize().padding(16.dp)){
  Text("Admin Dashboard",fontSize=22.sp,fontWeight=FontWeight.Bold)
  Text("Customer order details")
  if(orders.isEmpty())Text("No orders yet.")
  LazyColumn { items(orders.reversed()){o->
   Card(Modifier.fillMaxWidth().padding(vertical=5.dp),colors=CardDefaults.cardColors(containerColor=cardCream)){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text("Order #${o.number} • ₹${o.total}",fontWeight=FontWeight.Bold,fontSize=18.sp)
    Text("Name: ${o.customer}")
    Text("Mobile: ${o.phone}")
    Text("Type: ${o.fulfillment}")
    if(o.fulfillment=="Home Delivery")Text("Address: ${o.address}")
    Text("Items:")
    o.lines.forEach{Text("• ${it.food.name} × ${it.qty} — ₹${it.price*it.qty}")}
   }}
  }}
  Spacer(Modifier.height(8.dp))
  Text("Important: This starter version stores orders only in this app session on this phone. To receive orders placed from customers' separate phones, an online database and internet connection must be connected.",fontSize=12.sp)
 }
}
