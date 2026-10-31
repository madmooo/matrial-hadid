/* Private on-device SQLite through the Android bridge. */
function validateStore(d){
 if(!d||d.version!==1||!Array.isArray(d.items)||!Array.isArray(d.prices)||d.items.length>10000||d.prices.length>50000)throw Error('File backup motabar nist.');
 const ids=new Set(),pids=new Set(),dates=new Set();
 for(const i of d.items){if(!Number.isSafeInteger(i.id)||i.id<1||ids.has(i.id)||typeof i.name!=='string'||!i.name.trim()||i.name.length>120||typeof i.unit!=='string'||!i.unit.trim()||i.unit.length>40||typeof i.description!=='string'||i.description.length>1000||![0,1].includes(i.active))throw Error('Item namotabar ast.');ids.add(i.id)}
 for(const p of d.prices){const key=p.item_id+':'+p.day;if(!Number.isSafeInteger(p.id)||p.id<1||pids.has(p.id)||!ids.has(p.item_id)||!Number.isSafeInteger(p.amount)||p.amount<=0||p.amount>1e15||!['تومان','ریال','دلار','یورو'].includes(p.currency)||typeof p.day!=='string'||!/^\d{4}-\d{2}-\d{2}$/.test(p.day)||!Number.isFinite(Date.parse(p.day))||new Date(p.day).toISOString().slice(0,10)!==p.day||dates.has(key)||typeof p.note!=='string'||p.note.length>1000)throw Error('Gheymat ya tarikh namotabar ast.');pids.add(p.id);dates.add(key)}
 return d;
}
function readLocal(){return validateStore(JSON.parse(MayehNative.read()))}
function saveLocal(d){validateStore(d);let result=MayehNative.save(JSON.stringify(d));if(result!=='ok')throw Error(result)}
async function localApi(path,method='GET',b={}){
 if(path==='me')return {username:'Local',role:'manager'};
 let d=readLocal();
 if(path==='data')return {items:d.items,prices:d.prices.slice().sort((a,b)=>b.day.localeCompare(a.day)||b.id-a.id)};
 if(path==='items'){
  let i={id:method==='POST'?Math.max(0,...d.items.map(i=>i.id))+1:+b.id,name:String(b.name||'').trim(),unit:String(b.unit||'').trim(),description:String(b.description||''),active:method==='POST'?1:+!!b.active};
  if(method==='POST')d.items.push(i);else{let at=d.items.findIndex(x=>x.id===i.id);if(at<0)throw Error('Item peyda nashod.');d.items[at]=i}
 }else if(path==='prices'){
  if(method==='DELETE')d.prices=d.prices.filter(p=>p.id!==b.id);
  else{
   let item=d.items.find(i=>i.id===b.item_id);if(!item||(!item.active&&method==='POST'))throw Error('Item faal entekhab kon.');
   if(d.prices.some(p=>p.item_id===b.item_id&&p.day===b.day&&p.id!==b.id))throw Error('In tarikh gheymat dare. Rekord ghabli ra virayesh kon.');
   if(!/^\d+$/.test(String(b.amount)))throw Error('Mablagh motabar vared kon.');
   let p={id:method==='POST'?Math.max(0,...d.prices.map(p=>p.id))+1:+b.id,item_id:b.item_id,amount:Number(b.amount),currency:b.currency,day:b.day,note:String(b.note||'')};
   if(method==='POST')d.prices.push(p);else{let at=d.prices.findIndex(x=>x.id===p.id);if(at<0)throw Error('Gheymat peyda nashod.');d.prices[at]=p}
  }
 }else throw Error('Amaliat namotabar ast.');
 saveLocal(d);return {};
}
function backupMenu(){let d=document.createElement('dialog');d.innerHTML='<h2>Backup</h2><p>Backup ra jaye amn negah dar. Hazf app, etelaat rooye gooshi ra pak mikonad.</p><div class="manage-actions"><button id="export">Zakhire backup</button><button id="import">Bazyabi backup</button><button id="close">Bastan</button></div>';document.body.append(d);d.showModal();d.onclose=()=>d.remove();d.querySelector('#close').onclick=()=>d.close();d.querySelector('#export').onclick=()=>MayehNative.exportBackup();d.querySelector('#import').onclick=()=>MayehNative.importBackup()}
window.restoreBackup=function(json){try{let data=validateStore(JSON.parse(json));confirmAction('Bazyabi backup','Etelaat feli ba backup jaygozin mishavad. Edame midi?',async()=>{saveLocal(data);document.querySelectorAll('dialog').forEach(d=>d.close());selected=null;await refresh();toast('Backup bazyabi shod.')})}catch(e){toast(e.message)}};
window.closeTopDialog=function(){let dialogs=document.querySelectorAll('dialog[open]');if(dialogs.length){dialogs[dialogs.length-1].close();return true}return false};
