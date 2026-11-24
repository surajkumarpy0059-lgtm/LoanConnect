package com.lucknowitescafe.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
val menu=listOf(
Food("Budget Combo","Chai + Bun Makkhan",listOf(49)),Food("Budget Combo","Chai + Poha",listOf(49)),Food("Budget Combo","Fries + Chai",listOf(59)),Food("Budget Combo","Fries / Potato Bites + Chai + Coffee",listOf(79)),Food("Budget Combo","Chai & Paratha",listOf(59)),Food("Budget Combo","Chai & Chilla",listOf(59)),Food("Budget Combo","Chilla & Coffee",listOf(69)),Food("Budget Combo","Special Combo: Potato Bites, Fries & Chai/Coffee",listOf(120)),
Food("Breakfast","Masala Chai",listOf(10,15,20)),Food("Breakfast","Tandoori Chai",listOf(40)),Food("Breakfast","Coffee",listOf(40)),Food("Breakfast","Cold Coffee",listOf(80)),
Food("Snacks","Masala Maggie",listOf(50)),Food("Snacks","Butter Maggie",listOf(60)),Food("Snacks","Bread Pakoda",listOf(20)),Food("Snacks","Paneer Pakoda",listOf(30)),Food("Snacks","Samosa",listOf(20)),Food("Snacks","Bun Makkhan",listOf(30)),Food("Snacks","Pyaaz Pakoda",listOf(30)),Food("Snacks","French Fries",listOf(40)),Food("Snacks","Potato Bites",listOf(40)),Food("Snacks","Idli",listOf(30,60)),Food("Snacks","Burger",listOf(40)),Food("Snacks","Sandwich",listOf(50)),
Food("Lunch","Biryani",listOf(70,150)),Food("Lunch","Aloo Paratha",listOf(40)),Food("Lunch","Chhole Chawal",listOf(50)),Food("Lunch","Puri Sabji",listOf(50)),
Food("Health Conscious","Sprouts",listOf(20,30)),Food("Health Conscious","Poha",listOf(40,80)),Food("Health Conscious","Sweet Corn Mix Chilla",listOf(40)),Food("Health Conscious","Ragi Kachori",listOf(50)),Food("Evening Cravings","Veg Momos",listOf(40)),Food("Evening Cravings","Paneer Momos",listOf(50)))
val ink=Color(0xFF171717); val gold=Color(0xFFD6B56D); val cream=Color(0xFFFFF9EE)
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{CafeApp()}}}
@Composable fun CafeApp(){
 var admin by remember{mutableStateOf(false)}; var cart by remember{mutableStateOf(listOf<Line>())}; var orders by remember{mutableStateOf(listOf<String>())}; var pin by remember{mutableStateOf("")}; var login by remember{mutableStateOf(false)}; var msg by remember{mutableStateOf("")}
 MaterialTheme(colorScheme=lightColorScheme(primary=ink,secondary=gold,background=cream)){
 Column(Modifier.fillMaxSize().background(cream)){
 Column(Modifier.fillMaxWidth().background(ink).padding(18.dp)){Text("LUCKNOWITES CAFE",color=gold,fontSize=23.sp,fontWeight=FontWeight.Bold);Text("Chai • Snacks • Combos",color=Color.White)}
 Row(Modifier.fillMaxWidth().padding(8.dp)){Button(onClick={admin=false},modifier=Modifier.weight(1f)){Text("Customer")};Spacer(Modifier.width(8.dp));Button(onClick={login=true},modifier=Modifier.weight(1f)){Text("Admin")}}
 if(admin) AdminPage(orders) else CustomerPage(cart,{f,p->val i=cart.indexOfFirst{it.food.name==f.name&&it.price==p};cart=if(i<0)cart+Line(f,p,1) else cart.mapIndexed{n,l->if(n==i)l.copy(qty=l.qty+1)else l};msg="Added to cart"},{l->cart=cart.mapNotNull{if(it==l){if(it.qty>1)it.copy(qty=it.qty-1)else null}else it}},{if(cart.isNotEmpty()){val total=cart.sumOf{it.price*it.qty};orders=orders+"Order #${orders.size+1} • ₹$total";cart=emptyList();msg="Order saved on this device. Staff confirmation required."}})
 if(msg.isNotBlank())Text(msg,Modifier.padding(8.dp),color=ink)
 }
 if(login)AlertDialog(onDismissRequest={login=false},title={Text("Admin Login")},text={Column{Text("Demo PIN: 1234");OutlinedTextField(pin,{pin=it},label={Text("PIN")})}},confirmButton={TextButton(onClick={if(pin=="1234"){admin=true;login=false;pin=""}else msg="Incorrect PIN"}){Text("Login")}},dismissButton={TextButton(onClick={login=false}){Text("Cancel")}})
 }
}
@Composable fun CustomerPage(cart:List<Line>,add:(Food,Int)->Unit,remove:(Line)->Unit,order:()->Unit){
 val cats=menu.map{it.category}.distinct();var cat by remember{mutableStateOf(cats.first())}
 Column(Modifier.fillMaxSize()){
 ScrollableTabRow(selectedTabIndex=cats.indexOf(cat)){cats.forEach{Tab(cat==it,{cat=it},text={Text(it)})}}
 LazyColumn(Modifier.weight(1f).padding(horizontal=10.dp)){items(menu.filter{it.category==cat}){f->
 Card(Modifier.fillMaxWidth().padding(vertical=4.dp),shape=RoundedCornerShape(12.dp)){Column(Modifier.padding(12.dp)){Text(f.name,fontWeight=FontWeight.SemiBold);Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){f.prices.forEachIndexed{i,p->Button(onClick={add(f,p)}){Text(if(f.prices.size==1)"Add ₹$p" else "Option ${i+1} ₹$p")}}}}}
 }}
 val total=cart.sumOf{it.price*it.qty}
 Card(Modifier.fillMaxWidth().padding(8.dp)){Column(Modifier.padding(10.dp)){Text("Cart • ₹$total",fontWeight=FontWeight.Bold,fontSize=18.sp);cart.forEach{l->Row(verticalAlignment=Alignment.CenterVertically){Text("${l.food.name} × ${l.qty}  ₹${l.price*l.qty}",Modifier.weight(1f),fontSize=12.sp);TextButton(onClick={remove(l)}){Text("−")}}};Button(onClick=order,enabled=cart.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Place Order")}}}
 }}
@Composable fun AdminPage(orders:List<String>){Column(Modifier.fillMaxSize().padding(16.dp)){Text("Admin Dashboard",fontSize=22.sp,fontWeight=FontWeight.Bold);Text("Orders in this app session");if(orders.isEmpty())Text("No orders yet.");orders.forEach{Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Text(it,Modifier.padding(14.dp))}};Spacer(Modifier.height(12.dp));Text("Demo PIN: 1234");Text("Starter build only: orders are not synced online and may clear when the app closes. Secure login and payment integration are not connected.",fontSize=12.sp)}}
