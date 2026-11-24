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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Food(val category:String,val name:String,val prices:List<Int>)
data class Line(val food:Food,val price:Int,val qty:Int)
data class CafeOrder(val number:Int,val customer:String,val phone:String,val fulfillment:String,val address:String,val lines:List<Line>,val total:Int,val status:String="New")
val initialMenu=listOf(
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
 var menu by remember{mutableStateOf(initialMenu)}
 var orders by remember{mutableStateOf(listOf<CafeOrder>())}
 var pin by remember{mutableStateOf("")}
 var login by remember{mutableStateOf(false)}
 var msg by remember{mutableStateOf("")}
 MaterialTheme(colorScheme=lightColorScheme(primary=tea,secondary=gold,background=cream,surface=Color.White)){
  Column(Modifier.fillMaxSize().background(cream)){
   Image(
    painter=painterResource(R.drawable.cafe_banner),
    contentDescription="Lucknowites Cafe shop banner",
    modifier=Modifier.fillMaxWidth().height(135.dp),
    contentScale=ContentScale.Crop
   )
   Row(Modifier.fillMaxWidth().padding(8.dp)){Button(onClick={admin=false},colors=ButtonDefaults.buttonColors(containerColor=tea),modifier=Modifier.weight(1f)){Text("Customer")};Spacer(Modifier.width(8.dp));Button(onClick={login=true},colors=ButtonDefaults.buttonColors(containerColor=orange),modifier=Modifier.weight(1f)){Text("Admin")}}
   if(admin) AdminPage(orders,menu,{updated->orders=orders.map{if(it.number==updated.number)updated else it}},{menu=menu+it},{number->orders=orders.filterNot{it.number==number}})
   else CustomerPage(menu,cart,
    {f,p->val i=cart.indexOfFirst{it.food.name==f.name&&it.price==p};cart=if(i<0)cart+Line(f,p,1) else cart.mapIndexed{n,l->if(n==i)l.copy(qty=l.qty+1)else l};msg="Added to cart"},
    {l->cart=cart.mapNotNull{if(it==l){if(it.qty>1)it.copy(qty=it.qty-1)else null}else it}},
    {l->cart=cart.map{if(it.food.name==l.food.name&&it.price==l.price)it.copy(qty=it.qty+1)else it}},
    {name,phone,mode,address->
      val total=cart.sumOf{it.price*it.qty}
      orders=orders+CafeOrder((orders.maxOfOrNull{it.number}?:0)+1,name,phone,mode,address,cart,total)
      cart=emptyList();msg="Order placed successfully."
    })
   if(msg.isNotBlank())Text(msg,Modifier.padding(8.dp),color=ink)
  }
  if(login)AlertDialog(onDismissRequest={login=false},title={Text("Admin Login")},text={Column{Text("Enter admin password");OutlinedTextField(pin,{pin=it},label={Text("Password")},visualTransformation=PasswordVisualTransformation(),singleLine=true)}},confirmButton={TextButton(onClick={if(pin=="CAFE@LU"){admin=true;login=false;pin=""}else msg="Incorrect PIN"}){Text("Login")}},dismissButton={TextButton(onClick={login=false}){Text("Cancel")}})
 }
}

@Composable fun CustomerPage(menu:List<Food>,cart:List<Line>,add:(Food,Int)->Unit,remove:(Line)->Unit,increase:(Line)->Unit,place:(String,String,String,String)->Unit){
 val cats=menu.map{it.category}.distinct()
 var cat by remember{mutableStateOf(cats.firstOrNull()?:"Menu")}
 var checkout by remember{mutableStateOf(false)}
 var search by remember{mutableStateOf("")}
 var customer by remember{mutableStateOf("")}
 var phone by remember{mutableStateOf("")}
 var address by remember{mutableStateOf("")}
 var mode by remember{mutableStateOf("Pickup from Cafe")}
 Column(Modifier.fillMaxSize()){
  Column(Modifier.fillMaxWidth().background(cream).padding(horizontal=12.dp, vertical=6.dp), verticalArrangement=Arrangement.spacedBy(4.dp)){
   Text("Good food, good mood",fontSize=18.sp,fontWeight=FontWeight.Bold,color=ink)
   Text("Order your favourites from LUCKNOWITES CAFE",fontSize=11.sp,color=tea)
   OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Search chai, snacks, combos...")},singleLine=true,shape=RoundedCornerShape(14.dp))
  }
  if(cats.isNotEmpty())ScrollableTabRow(selectedTabIndex=cats.indexOf(cat).coerceAtLeast(0), edgePadding=8.dp){cats.forEach{Tab(cat==it,{cat=it},text={Text(it,fontSize=12.sp)})}}
  LazyColumn(Modifier.weight(1f).padding(horizontal=10.dp)){items(menu.filter{(search.isNotBlank() || it.category==cat) && (search.isBlank() || it.name.contains(search,ignoreCase=true) || it.category.contains(search,ignoreCase=true))}){f->
   Card(Modifier.fillMaxWidth().padding(vertical=3.dp),colors=CardDefaults.cardColors(containerColor=cardCream),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(10.dp)){Text(f.name,fontWeight=FontWeight.Bold,color=ink,fontSize=15.sp);Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){f.prices.forEachIndexed{i,p->Button(onClick={add(f,p)},colors=ButtonDefaults.buttonColors(containerColor=leaf)){Text(if(f.prices.size==1)"Add ₹$p" else "Option ${i+1} ₹$p")}}}}}
  }}
  val total=cart.sumOf{it.price*it.qty}
  Card(Modifier.fillMaxWidth().padding(8.dp)){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){Text("Cart • ₹$total",fontWeight=FontWeight.Bold,fontSize=18.sp,modifier=Modifier.weight(1f));TextButton(onClick={cart.toList().forEach{l->repeat(l.qty){remove(l.copy(qty=1))}}}){Text("Clear cart",color=Color.Red)}}
   cart.forEach{l->Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(3.dp)){
    Text("${l.food.name}  ₹${l.price*l.qty}",Modifier.weight(1f),fontSize=12.sp)
    TextButton(onClick={remove(l)}){Text("−")}
    Text("${l.qty}",fontWeight=FontWeight.Bold)
    TextButton(onClick={increase(l)}){Text("+")}
    TextButton(onClick={repeat(l.qty){remove(l.copy(qty=1))}}){Text("Remove",color=Color.Red)}
   }}
   Button(onClick={checkout=true},enabled=cart.isNotEmpty(),colors=ButtonDefaults.buttonColors(containerColor=orange),modifier=Modifier.fillMaxWidth()){Text("Continue to Checkout")}
  }}
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
 },confirmButton={TextButton(onClick={if(customer.isNotBlank()&&phone.length>=10&&(mode=="Pickup from Cafe"||address.isNotBlank())){place(customer.trim(),phone.trim(),mode,address.trim());checkout=false}}){Text("Place Order")}},dismissButton={TextButton(onClick={checkout=false}){Text("Cancel")}})
}

@Composable fun AdminPage(orders:List<CafeOrder>,menu:List<Food>,change:(CafeOrder)->Unit,addFood:(Food)->Unit,delete:(Int)->Unit){
 var tab by remember{mutableStateOf("New")}
 var addDialog by remember{mutableStateOf(false)}
 var foodName by remember{mutableStateOf("")}
 var foodPrice by remember{mutableStateOf("")}
 var foodCategory by remember{mutableStateOf("Snacks")}
 var pendingDelete by remember{mutableStateOf<Int?>(null)}
 val categories=listOf("Budget Combo","Breakfast","Snacks","Lunch","Health Conscious","Evening Cravings")
 val visible=when(tab){"New"->orders.filter{it.status=="New"};"Completed"->orders.filter{it.status=="Completed"};"Cancelled"->orders.filter{it.status=="Cancelled"};else->orders}
 Column(Modifier.fillMaxSize().padding(14.dp)){
  Text("Admin Dashboard",fontSize=23.sp,fontWeight=FontWeight.Bold)
  Text("Manage orders, history and menu")
  Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("New","Completed","Cancelled","All History").forEach{t->FilterChip(selected=tab==t,onClick={tab=t},label={Text(t)})}}
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Orders: ${orders.size}",Modifier.weight(1f));Button(onClick={addDialog=true},colors=ButtonDefaults.buttonColors(containerColor=orange)){Text("+ Add Food")}}
  if(visible.isEmpty())Text(if(tab=="New")"No new orders." else "No orders in this section.")
  LazyColumn(Modifier.weight(1f)){items(visible.reversed()){o->
   Card(Modifier.fillMaxWidth().padding(vertical=5.dp),colors=CardDefaults.cardColors(containerColor=cardCream)){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text("Order #${o.number} • ₹${o.total}",fontWeight=FontWeight.Bold,fontSize=18.sp)
    Text("Status: ${o.status}",fontWeight=FontWeight.Bold,color=when(o.status){"Completed"->leaf;"Cancelled"->Color.Red;else->tea})
    Text("Name: ${o.customer}");Text("Mobile: ${o.phone}");Text("Type: ${o.fulfillment}")
    if(o.fulfillment=="Home Delivery")Text("Address: ${o.address}")
    Text("Items:")
    o.lines.forEach{Text("• ${it.food.name} × ${it.qty} — ₹${it.price*it.qty}")}
    if(o.status=="New")Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
      Button(onClick={change(o.copy(status="Completed"))},colors=ButtonDefaults.buttonColors(containerColor=leaf)){Text("Mark Completed")}
      OutlinedButton(onClick={change(o.copy(status="Cancelled"))}){Text("Cancel Order")}
    }
    OutlinedButton(onClick={pendingDelete=o.number},colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.Red),modifier=Modifier.fillMaxWidth()){Text("Delete Order")}
   }}
  }}
  Text("Menu items: ${menu.size} • Added food appears in Customer menu.",fontSize=12.sp)
  Text("Note: Orders and menu changes are currently stored in this app session on this phone. Online customer-to-admin sync and permanent history need a connected database.",fontSize=11.sp,color=tea)
 }
 if(pendingDelete!=null)AlertDialog(onDismissRequest={pendingDelete=null},title={Text("Delete order?")},text={Text("Order #${pendingDelete} will be permanently removed from this app session.")},confirmButton={TextButton(onClick={pendingDelete?.let{delete(it)};pendingDelete=null}){Text("Delete",color=Color.Red)}},dismissButton={TextButton(onClick={pendingDelete=null}){Text("Keep")}})
 if(addDialog)AlertDialog(onDismissRequest={addDialog=false},title={Text("Add New Food")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
  OutlinedTextField(foodName,{foodName=it},label={Text("Food name")},singleLine=true)
  OutlinedTextField(foodPrice,{foodPrice=it.filter{c->c.isDigit()}},label={Text("Price (₹)")},singleLine=true)
  Text("Category")
  categories.forEach{c->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=foodCategory==c,onClick={foodCategory=c});Text(c)}}
 }},confirmButton={TextButton(onClick={val p=foodPrice.toIntOrNull();if(foodName.isNotBlank()&&p!=null&&p>0){addFood(Food(foodCategory,foodName.trim(),listOf(p)));foodName="";foodPrice="";addDialog=false}}){Text("Add Food")}},dismissButton={TextButton(onClick={addDialog=false}){Text("Cancel")}})
}
