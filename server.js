const express = require("express");
const session = require("express-session");
const crypto = require("crypto");
const path = require("path");
require("dotenv").config();

const app = express();
app.use(express.json({limit:"1mb"}));
app.use(session({
  secret: process.env.SESSION_SECRET || "CHANGE_THIS_IN_PRODUCTION",
  resave:false,
  saveUninitialized:false,
  cookie:{httpOnly:true,sameSite:"lax",secure:false,maxAge:1000*60*60*8}
}));

const ADMIN_EMAIL = process.env.ADMIN_EMAIL || "owner@example.com";
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD || "CHANGE_ME";

function adminOnly(req,res,next){
  if(req.session && req.session.isAdmin) return next();
  return res.status(401).json({message:"Admin login required"});
}

app.post("/api/admin/login",(req,res)=>{
  const {email,password}=req.body||{};
  if(email===ADMIN_EMAIL && password===ADMIN_PASSWORD){
    req.session.isAdmin=true;
    return res.json({ok:true});
  }
  return res.status(401).json({message:"Invalid owner credentials"});
});
app.post("/api/admin/logout",(req,res)=>req.session.destroy(()=>res.json({ok:true})));

app.post("/api/cibil/report", async (req,res)=>{
  if(!req.body?.consent) return res.status(400).json({message:"Customer consent is required"});
  // Intentionally NO fake score. A real CIBIL/authorized-provider API contract
  // must be configured here after your organization has the required authorization.
  if(!process.env.CIBIL_API_URL || !process.env.CIBIL_API_TOKEN){
    return res.status(503).json({
      message:"Authorized CIBIL provider is not configured on this server."
    });
  }
  try{
    const response = await fetch(process.env.CIBIL_API_URL,{
      method:"POST",
      headers:{
        "Content-Type":"application/json",
        "Authorization":"Bearer "+process.env.CIBIL_API_TOKEN
      },
      body:JSON.stringify(req.body)
    });
    const text=await response.text();
    res.status(response.status).type("application/json").send(text);
  }catch(e){
    res.status(502).json({message:"CIBIL provider request failed"});
  }
});

app.use(express.static(path.join(__dirname)));
app.listen(process.env.PORT||3000,()=>console.log("LoanConnect running on port "+(process.env.PORT||3000)));
