
	const seats_menu = document.getElementById("seat");
	for (let i = 0; i < 50; i++){//50 number of seats
		const option = document.createElement("option");
		option.text = (i + 1).toString();
		option.value = (i + 1).toString();
		seats_menu.appendChild(option);
	}


	var ticket_price=0;
	function price(){
		const travel_class = document.querySelector("#travel_class").value;
		if(travel_class ==="Economy class"){
			ticket_price = 250;
		}
		else if(travel_class ==="Premium Economy class"){
			ticket_price = 350;
		}
		else if(travel_class ==="Business class"){
			ticket_price = 700;
		}
		else if(travel_class ==="First class"){
			ticket_price = 1000;
		}
		var priceDue = "The ticket price is:"+ ticket_price+"$";
		alert(priceDue);

	}
	document.write("hi");
		