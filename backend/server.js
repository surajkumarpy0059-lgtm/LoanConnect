import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import Database from 'better-sqlite3';
import path from 'path';
import { fileURLToPath } from 'url';

const app=express();
const db=new Database(process.env.DB_FILE||'loanconnect.db');
const PORT=process.env.PORT||8080;
const JWT_SECRET=process.env.JWT_SECRET||'CHANGE_THIS_SECRET_IN_PRODUCTION';
const ADMIN_MOBILE=process.env.ADMIN_MOBILE||'';
const ADMIN_PIN_HASH=process.env.ADMIN_PIN_HASH||'';
const __filename=fileURLToPath(import.meta.url);
const __dirname=path.dirname(__filename);
app.use(cors());
app.use(express.json({limit:'1mb'}));

// Serve the browser app from ../web so one URL works on desktop, Android and iPhone.
app.use(express.static(path.join(__dirname,'../web')));

db.exec(`CREATE TABLE IF NOT EXISTS customers(mobile TEXT PRIMARY KEY, created_at TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS applications(id TEXT PRIMARY KEY,name TEXT NOT NULL,mobile TEXT NOT NULL,pan TEXT,income INTEGER,amount INTEGER NOT NULL,loan TEXT NOT NULL,bank TEXT,status TEXT NOT NULL,created_at TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS lenders(id TEXT PRIMARY KEY,name TEXT NOT NULL,active INTEGER NOT NULL DEFAULT 1);
CREATE TABLE IF NOT EXISTS cibil_requests(id TEXT PRIMARY KEY,mobile TEXT NOT NULL,pan TEXT NOT NULL,status TEXT NOT NULL,created_at TEXT NOT NULL);`);

const now=()=>new Date().toISOString();
const token=(role,mobile)=>jwt.sign({role,mobile},JWT_SECRET,{expiresIn:'12h'});
function auth(role){return (req,res,next)=>{try{const t=(req.headers.authorization||'').replace(/^Bearer\s+/,'');const p=jwt.verify(t,JWT_SECRET);if(role&&p.role!==role)return res.status(403).json({error:'Forbidden'});req.user=p;next()}catch(e){res.status(401).json({error:'Unauthorized'})}}}

app.get('/api/health',(req,res)=>res.json({ok:true,service:'LoanConnect API'}));
app.post('/api/customer/request-otp',(req,res)=>{const mobile=String(req.body.mobile||'');if(!/^\d{10}$/.test(mobile))return res.status(400).json({error:'Invalid mobile'});res.json({ok:true,message:'OTP provider must be connected for production'});});
app.post('/api/customer/verify-otp',async(req,res)=>{const mobile=String(req.body.mobile||'');const otp=String(req.body.otp||'');if(!/^\d{10}$/.test(mobile))return res.status(400).json({error:'Invalid mobile'});if(!process.env.DEMO_OTP||otp!==process.env.DEMO_OTP)return res.status(401).json({error:'OTP verification unavailable or invalid'});db.prepare('INSERT OR IGNORE INTO customers(mobile,created_at) VALUES(?,?)').run(mobile,now());res.json({token:token('customer',mobile),mobile});});
app.post('/api/admin/login',async(req,res)=>{const mobile=String(req.body.mobile||'');const pin=String(req.body.pin||'');if(!ADMIN_MOBILE||!ADMIN_PIN_HASH)return res.status(503).json({error:'Admin credentials are not configured'});if(mobile!==ADMIN_MOBILE||!(await bcrypt.compare(pin,ADMIN_PIN_HASH)))return res.status(401).json({error:'Invalid admin credentials'});res.json({token:token('admin',mobile)});});

app.get('/api/lenders',(req,res)=>res.json(db.prepare('SELECT id,name,active FROM lenders ORDER BY name').all()));
app.post('/api/applications',auth('customer'),(req,res)=>{const {name,mobile,pan,income,amount,loan,bank}=req.body;if(mobile!==req.user.mobile||!name||!amount||!loan)return res.status(400).json({error:'Required fields missing'});const id='LC'+Date.now().toString().slice(-8);db.prepare('INSERT INTO applications VALUES(?,?,?,?,?,?,?,?,?,?)').run(id,name,mobile,pan||'',Number(income||0),Number(amount),loan,bank||'','Applied',now());db.prepare('INSERT OR IGNORE INTO customers(mobile,created_at) VALUES(?,?)').run(mobile,now());res.status(201).json({id,status:'Applied'});});
app.get('/api/applications/me',auth('customer'),(req,res)=>res.json(db.prepare('SELECT id,name,mobile,pan,income,amount,loan,bank,status,created_at FROM applications WHERE mobile=? ORDER BY created_at DESC').all(req.user.mobile)));
app.post('/api/cibil-requests',auth('customer'),(req,res)=>{const pan=String(req.body.pan||'').toUpperCase();if(!/^[A-Z]{5}[0-9]{4}[A-Z]$/.test(pan))return res.status(400).json({error:'Invalid PAN'});const id='CB'+Date.now().toString().slice(-8);db.prepare('INSERT INTO cibil_requests VALUES(?,?,?,?,?)').run(id,req.user.mobile,pan,'Requested',now());res.status(201).json({id,status:'Requested',message:'Authorized CIBIL provider integration required'});});

app.get('/api/admin/dashboard',auth('admin'),(req,res)=>res.json({applications:db.prepare('SELECT COUNT(*) AS c FROM applications').get().c,customers:db.prepare('SELECT COUNT(*) AS c FROM customers').get().c,pending:db.prepare("SELECT COUNT(*) AS c FROM applications WHERE status IN ('Applied','Under Process')").get().c,approved:db.prepare("SELECT COUNT(*) AS c FROM applications WHERE status='Approved'").get().c,cibil:db.prepare('SELECT COUNT(*) AS c FROM cibil_requests').get().c}));
app.get('/api/admin/applications',auth('admin'),(req,res)=>res.json(db.prepare('SELECT * FROM applications ORDER BY created_at DESC').all()));
app.patch('/api/admin/applications/:id',auth('admin'),(req,res)=>{const allowed=['Applied','Under Process','Approved','Rejected','Disbursed'];const s=String(req.body.status||'');if(!allowed.includes(s))return res.status(400).json({error:'Invalid status'});const r=db.prepare('UPDATE applications SET status=? WHERE id=?').run(s,req.params.id);res.json({ok:r.changes>0});});
app.delete('/api/admin/applications/:id',auth('admin'),(req,res)=>{db.prepare('DELETE FROM applications WHERE id=?').run(req.params.id);res.json({ok:true});});
app.get('/api/admin/customers',auth('admin'),(req,res)=>res.json(db.prepare('SELECT c.mobile,c.created_at,COUNT(a.id) applications FROM customers c LEFT JOIN applications a ON a.mobile=c.mobile GROUP BY c.mobile ORDER BY c.created_at DESC').all()));
app.get('/api/admin/cibil',auth('admin'),(req,res)=>res.json(db.prepare('SELECT * FROM cibil_requests ORDER BY created_at DESC').all()));
app.patch('/api/admin/cibil/:id',auth('admin'),(req,res)=>{const s=String(req.body.status||'');if(!['Requested','Checked','Failed'].includes(s))return res.status(400).json({error:'Invalid status'});db.prepare('UPDATE cibil_requests SET status=? WHERE id=?').run(s,req.params.id);res.json({ok:true});});
app.post('/api/admin/lenders',auth('admin'),(req,res)=>{const name=String(req.body.name||'').trim();if(!name)return res.status(400).json({error:'Name required'});const id='B'+Date.now();db.prepare('INSERT INTO lenders VALUES(?,?,1)').run(id,name);res.status(201).json({id,name,active:1});});
app.patch('/api/admin/lenders/:id',auth('admin'),(req,res)=>{db.prepare('UPDATE lenders SET active=? WHERE id=?').run(req.body.active?1:0,req.params.id);res.json({ok:true});});
app.delete('/api/admin/lenders/:id',auth('admin'),(req,res)=>{db.prepare('DELETE FROM lenders WHERE id=?').run(req.params.id);res.json({ok:true});});

app.get('*',(req,res)=>res.sendFile(path.join(__dirname,'../web/index.html')));
app.listen(PORT,()=>console.log(`LoanConnect API listening on ${PORT}`));
