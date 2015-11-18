if (typeof Protoplasm == "undefined") {
	var Protoplasm = function() {
		var b = "1.7.0.0";
		var g = "1.8.3";
		var h = "https://ajax.googleapis.com/ajax/libs/prototype/" + b
				+ "/prototype.js";
		var d = "https://ajax.googleapis.com/ajax/libs/scriptaculous/" + g
				+ "/";
		var e = source = loaded = failed = used = false;
		var i;
		var c = [];
		var a = [];
		var f = {};
		return {
			Version : "0.1",
			base : function(j) {
				return i + j + "/"
			},
			load : function() {
				function j(l) {
					var m = l.replace(/_.*|\./g, "");
					m = parseInt(m + "0".times(4 - m.length));
					return l.indexOf("_") > -1 ? m - 1 : m
				}
				failed = false;
				if (typeof Prototype == "undefined") {
					Protoplasm.require(h, Protoplasm.load);
					return
				} else {
					if (j(Prototype.Version) < j(b)) {
						failed = true;
						throw ("Protoplasm requires the Prototype JavaScript framework >= " + b)
					}
				}
				var k = /protoplasm(_[a-z]*)?\.js(\?.*)?$/;
				$$("head script[src]").findAll(function(l) {
							return l.src.match(k)
						}).each(function(m) {
					var n = m.src.match(k), l = m.src
							.match(/\?.*load=([a-z,]*)/);
					i = m.src.replace(k, "");
					loaded = true;
					if (n[1] == "_full") {
						e = true;
						Protoplasm.loadStylesheet(i + "protoplasm_full.css");
						return
					}
					if (n[1] == "_src") {
						source = true
					}
					var o = (l ? l[1].split(",") : a);
					if (o) {
						o.each(Protoplasm.use)
					}
				})
			},
			loadStylesheet : function(k, l) {
				if (l) {
					k = e ? i + "/protoplasm_full.css" : i + l + "/" + k
				}
				if (!$$("head link[rel=stylesheet]").find(function(m) {
							return m.href == k
						})) {
					var j = new Element("link", {
								rel : "stylesheet",
								type : "text/css",
								href : k
							});
					$$("head")[0].appendChild(j)
				}
			},
			register : function(k, j) {
				f[k] = j
			},
			require : function(m, o) {
				if (c.indexOf(m) > -1) {
					if (o) {
						o()
					}
					return
				}
				c.push(m);
				try {
					if (document.loaded) {
						throw ("Already loaded")
					}
					document.write('<script type="text/javascript" src="' + m
							+ '"><\/script>');
					if (o) {
						var j = "_script_onload_" + c.length;
						window[j] = o;
						document.write('<script type="text/javascript">');
						document.write("window." + j + "(); delete window." + j
								+ ";");
						document.write("<\/script>")
					}
				} catch (n) {
					var k = false;
					var l = new Element("script", {
								type : "text/javascript",
								src : m
							});
					if (o) {
						l.onload = l.onreadystatechange = function() {
							var p = this.readyState;
							if (k || (p && p != "complete" && p != "loaded")) {
								return
							}
							k = true;
							o()
						}
					}
					$$("head")[0].appendChild(l)
				}
			},
			transform : function(l, k) {
				var m = Array.prototype.slice.call(arguments, 2);
				function j(n) {
					if (l in f) {
						$$(k).each(function(o) {
									var p = new f[l](o, m[0], m[1], m[2], m[3])
								})
					}
				}
				if (document.loaded) {
					j()
				} else {
					document.on("dom:loaded", j)
				}
				return new Protoplasm.Transformer(l)
			},
			use : function(j, k) {
				if (Object.isArray(j)) {
					j.each(Protoplasm.use);
					return
				}
				if (failed) {
					throw ("Protoplasm loading failed, cannot include controls")
				} else {
					if (!loaded) {
						a.push(j)
					} else {
						if (!e && !(j in f)) {
							used
									|| Protoplasm.loadStylesheet(i
											+ "protoplasm.css");
							used = true;
							Protoplasm
									.require(i + j + "/" + j
													+ (source ? "_src" : "")
													+ ".js", k)
						}
					}
				}
				return new Protoplasm.Transformer(j)
			},
			useScriptaculous : function(j, k) {
				Protoplasm.require(d + j + ".js", k)
			},
			Transformer : function(j) {
				return {
					use : Protoplasm.use,
					transform : function() {
						var k = Array.prototype.slice.call(arguments, 0);
						k.unshift(j);
						return Protoplasm.transform.apply(Protoplasm, k)
					}
				}
			},
			extend : function(k, j) {
				k = $(k);
				j = $H(j);
				j.each(function(l) {
							if (l.key in k) {
								k["_" + l.key] = k[l.key]
							}
							k[l.key] = l.value
						});
				k.store("_extensions", j);
				return k
			},
			revert : function(k) {
				k = $(k);
				var j = k.retrieve("_extensions");
				if (j) {
					j.each(function(l) {
								if ("_" + l.key in k) {
									k[l.key] = k["_" + l.key];
									k["_" + l.key] = null
								} else {
									k[l.key] = null
								}
							})
				}
				return k
			}
		}
	}()
}
Protoplasm.load();