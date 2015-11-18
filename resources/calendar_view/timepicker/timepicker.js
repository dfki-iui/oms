if (typeof Protoplasm == "undefined") {
	throw ("protoplasm.js not loaded, could not intitialize timepicker")
}
if (typeof Control == "undefined") {
	Control = {}
}
Protoplasm.loadStylesheet("../../resources/calendar_view/timepicker/timepicker.css", "timepicker");
Control.TimePicker = Class.create({
			initialize : function(b, a) {
				b = $(b);
				if (tp = b.retrieve("timepicker")) {
					tp.destroy()
				}
				var c = b.wrap("span", {
							style : "position:relative;"
						});
				a = Object.extend({
							format : "HH:mm:ss"
						}, a || {});
				if (!a.icon) {
					a.icon = Protoplasm.base("timepicker") + "../../resources/calendar_view/timepicker/clock.png"
				}
				this.element = b;
				this.label = b;
				this.wrapper = c;
				this.options = a;
				this.changeHandler = a.onChange;
				this.selectHandler = a.onSelect;
				a.onSelect = this.onSelect.bind(this);
				a.onChange = this.onChange.bind(this);
				this.panel = null;
				this.dialog = null;
				this.listeners = [
						b.on("click", this.toggle.bindAsEventListener(this)),
						b.on("keydown", this.keyHandler
										.bindAsEventListener(this)),
						Event.on(window, "unload", this.destroy.bind(this))];
				if (a.icon) {
					b.style.background = "url(" + a.icon
							+ ") right center no-repeat #FFF";
					this.oldPadding = b.style.paddingRight;
					b.style.paddingRight = "20px"
				}
				this.hideListener = null;
				this.keyListener = null;
				this.active = false;
				this.element.store("timepicker", this);
				this.element = Protoplasm.extend(b, {
							show : c.show.bind(c),
							hide : c.hide.bind(c),
							open : this.open.bind(this),
							toggle : this.toggle.bind(this),
							close : this.close.bind(this),
							destroy : this.destroy.bind(this)
						})
			},
			destroy : function() {
				Protoplasm.revert(this.element);
				this.listeners.invoke("stop");
				if (this.hideListener) {
					this.hideListener.stop()
				}
				if (this.keyListener) {
					this.keyListener.stop()
				}
				this.wrapper.parentNode
						.replaceChild(this.element, this.wrapper);
				this.element.style.paddingRight = this.oldPadding;
				this.element.store("timepicker", null)
			},
			clickHandler : function(b) {
				var a = Event.element(b);
				do {
					if (a == this.element || a == this.label
							|| a == this.dialog) {
						return
					}
				} while (a = a.parentNode);
				this.close()
			},
			setValue : function(g) {
				var f = g.getHours();
				var b = g.getMinutes();
				var e = g.getSeconds();
				var c = "";
				if (!this.options.use24hrs) {
					c = " AM";
					if (f == 0) {
						f = 12
					} else {
						if (f > 11) {
							if (f > 12) {
								f -= 12
							}
							c = " PM"
						}
					}
				}
				f = f < 10 ? "0" + f : f;
				b = b < 10 ? "0" + b : b;
				e = e < 10 ? "0" + e : e;
				this.element.value = f + ":" + b + ":" + e + c
			},
			onSelect : function(a) {
				this.setValue(a);
				this.close();
				if (this.selectHandler) {
					this.selectHandler(a)
				}
			},
			onChange : function(a) {
				this.setValue(a);
				if (this.changeHandler) {
					this.changeHandler(a)
				}
			},
			toggle : function(a) {
				if (this.active) {
					this.close()
				} else {
					setTimeout(this.open.bind(this))
				}
			},
			keyHandler : function(a) {
				switch (a.keyCode) {
					case Event.KEY_ESC :
						this.close();
						return;
					case Event.KEY_RETURN :
						this.close();
						return;
					case Event.KEY_TAB :
						this.close();
						return;
					case Event.KEY_DOWN :
						if (!this.dialog || !this.dialog.parentNode) {
							this.open();
							Event.stop(a)
						}
				}
				if (this.pickerActive) {
					return false
				}
			},
			docKeyHandler : function(a) {
				switch (a.keyCode) {
					case Event.KEY_ESC :
						this.close();
						return;
					case Event.KEY_RETURN :
						this.close();
						Event.stop(a);
						return
				}
				if (this.pickerActive) {
					return false
				}
			},
			open : function() {
				if (!this.active) {
					if (!this.dialog) {
						this.panel = new Control.TimePicker.Panel(this.options);
						this.dialog = new Element("div", {
									"class" : "_pp_frame_small",
									style : "position:absolute;"
								});
						this.dialog.insert(this.panel.element)
					}
					var b = this.label.getLayout();
					var a = b.get("border-box-height") - b.get("border-bottom");
					document.body.appendChild(this.dialog);
					this.dialog.clonePosition(this.label, {
								setWidth : false,
								setHeight : false,
								offsetTop : a,
								offsetLeft : -3
							});
					this.dialog.style.zIndex = "99";
					this.panel.setTime(this.parse(this.element.value));
					this.panel.hours.focus();
					this.hideListener = document.on("click", this.clickHandler
									.bindAsEventListener(this));
					this.keyListener = document.on("keydown",
							this.docKeyHandler.bindAsEventListener(this));
					this.active = true
				}
			},
			parse : function(g) {
				var f, e, a, c, b;
				if (!this.options.use24hrs) {
					f = g.split(/ /);
					if (f.length > 1) {
						b = f[1].toUpperCase()
					}
					g = f[0]
				}
				f = g.split(":");
				e = f[0] || 0;
				a = f[1] || 0;
				c = f.length > 2 ? f[2] : 0;
				if (!this.options.use24hrs) {
					if (b == "AM" && e == 12) {
						e = 0
					} else {
						if (e < 12 && b == "PM") {
							e += 12
						}
					}
				}
				d = new Date();
				d.setHours(e);
				d.setMinutes(a);
				d.setSeconds(c);
				return d
			},
			close : function() {
				if (this.active) {
					this.dialog.remove();
					this.active = false;
					if (this.hideListener) {
						this.hideListener.stop()
					}
					if (this.keyListener) {
						this.keyListener.stop()
					}
				}
			}
		});
Control.TimePicker.Panel = Class.create({
			initialize : function(a) {
				this.options = Object.extend({}, a || {});
				this.time = this.options.time || new Date();
				this.ampm = "AM";
				this.element = this.createPicker();
				this.element.on("selectstart", function(b) {
							Event.stop(b)
						}.bindAsEventListener(this))
			},
			createPicker : function() {
				var a = new Element("div", {
							"class" : "_pp_timepicker "
									+ this.options.className
						});
				this.hours = new Element("input", {
							type : "text",
							"class" : "_pp_timepicker_input",
							value : "00"
						});
				this.minutes = new Element("input", {
							type : "text",
							"class" : "_pp_timepicker_input",
							value : "00"
						});
				this.seconds = new Element("input", {
							type : "text",
							"class" : "_pp_timepicker_input",
							value : "00"
						});
				var b = new Element("table", {
							cellPadding : 0,
							cellSpacing : 0,
							border : 0
						});
				var c = b.insertRow(0);
				c.appendChild(this.createCell(this.hours, this.options.use24hrs
								? 23
								: 12, this.options.use24hrs ? 0 : 1));
				c.appendChild(new Element("td").update(":"));
				c.appendChild(this.createCell(this.minutes, 59, 0));
				c.appendChild(new Element("td").update(":"));
				c.appendChild(this.createCell(this.seconds, 59, 0));
				if (!this.options.use24hrs) {
					var e = new Element("td");
					this.am = new Element("div", {
								"class" : "_pp_timepicker_ampm _pp_highlight"
							}).update("AM");
					this.am.on("click", function() {
								this.ampm = "AM";
								this.pm.removeClassName("_pp_highlight");
								this.am.addClassName("_pp_highlight");
								this.onChange()
							}.bindAsEventListener(this));
					this.pm = new Element("div", {
								"class" : "_pp_timepicker_ampm"
							}).update("PM");
					this.pm.on("click", function() {
								this.ampm = "PM";
								this.am.removeClassName("_pp_highlight");
								this.pm.addClassName("_pp_highlight");
								this.onChange()
							}.bindAsEventListener(this));
					e.appendChild(this.am);
					e.appendChild(this.pm);
					c.appendChild(e)
				}
				a.appendChild(b);
				return a
			},
			createCell : function(b, e, c) {
				var g = new Element("td");
				b.on("keydown", function(h) {
							if (h.keyCode == Event.KEY_UP) {
								this.increment(b, e, c);
								this.onChange();
								Event.stop(h)
							} else {
								if (h.keyCode == Event.KEY_DOWN) {
									this.decrement(b, e, c);
									this.onChange();
									Event.stop(h)
								} else {
									if (h.keyCode == Event.KEY_RETURN) {
										this.onSelect();
										Event.stop(h);
										return false
									}
								}
							}
						}.bindAsEventListener(this));
				b.on("change", function(i) {
							var h = b.value * 1;
							b.value = h < 10 ? "0" + h : h;
							this.onChange()
						}.bindAsEventListener(this));
				b.on("focus", function(h) {
							b.select()
						}.bindAsEventListener(this));
				var a = new Element("div", {
							"class" : "_pp_highlight _pp_timepicker_up"
						});
				this.setBehavior(a, b, this.increment, e, c);
				var f = new Element("div", {
							"class" : "_pp_highlight _pp_timepicker_down"
						});
				this.setBehavior(f, b, this.decrement, e, c);
				g.appendChild(a);
				g.appendChild(b);
				g.appendChild(f);
				return g
			},
			setBehavior : function(c, b, g, h, f) {
				var e = false;
				var a = (h - f > 30) ? 5 : 1;
				c.on("mousedown", function(j) {
							e = true;
							var i;
							setTimeout(function() {
										if (e) {
											i = setInterval(function() {
														if (e) {
															g(b, h, f, a);
															this.onChange()
														} else {
															clearInterval(i)
														}
													}.bind(this), 200)
										}
									}.bind(this), 500);
							g(b, h, f);
							this.onChange()
						}.bindAsEventListener(this));
				c.on("mouseup", function(i) {
							e = false
						}.bindAsEventListener(this));
				c.on("mouseout", function(i) {
							e = false
						}.bindAsEventListener(this))
			},
			increment : function(b, f, c, a) {
				if (!a) {
					a = 1
				}
				var e = b.value * 1;
				e += a;
				e = e - e % a;
				if (e > f) {
					e = c
				}
				b.value = e < 10 ? "0" + e : e
			},
			decrement : function(b, f, c, a) {
				if (!a) {
					a = 1
				}
				var e = b.value * 1;
				e -= a;
				e = e - e % a;
				if (e < c) {
					e = f
				}
				b.value = e < 10 ? "0" + e : e
			},
			onChange : function() {
				if (this.options.onChange) {
					this.options.onChange(this.getTime())
				}
			},
			onSelect : function() {
				if (this.options.onSelect) {
					this.options.onSelect(this.getTime())
				}
			},
			getTime : function() {
				var b = new Date();
				var a = this.hours.value * 1;
				if (!this.options.use24hrs) {
					if (a == 12 && this.ampm == "AM") {
						a = 0
					} else {
						if (a < 12 && this.ampm == "PM") {
							a += 12
						}
					}
				}
				b.setHours(a);
				b.setMinutes(this.minutes.value * 1);
				b.setSeconds(this.seconds.value * 1);
				return b
			},
			setTime : function(e) {
				var c = e.getHours();
				var a = e.getMinutes();
				var b = e.getSeconds();
				if (!this.options.use24hrs) {
					this.ampm = "AM";
					this.am.addClassName("_pp_highlight");
					this.pm.removeClassName("_pp_highlight");
					if (c > 12) {
						c -= 12;
						this.ampm = "PM";
						this.pm.addClassName("_pp_highlight");
						this.am.removeClassName("_pp_highlight")
					} else {
						if (c == 0) {
							c = 12
						}
					}
				}
				this.hours.value = c >= 10 ? c : "0" + c;
				this.minutes.value = a >= 10 ? a : "0" + a;
				this.seconds.value = b >= 10 ? b : "0" + b
			}
		});
Protoplasm.register("timepicker", Control.TimePicker);