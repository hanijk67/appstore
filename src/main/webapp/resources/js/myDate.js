function MyDate(year, month, day, hour, minute, second) {
    this.incSec = function() {
        second++;
        if (second >= 60) {
            this.incMin();
            second = 0;
        }
    };

    this.incMin = function() {
        minute++;
        if (minute >= 60) {
            this.incHour();
            minute = 0;
        }
    };

    this.incHour = function() {
        hour++;
        if (hour >= 24) {
            this.incDay();
            hour = 0;
        }
    };

    this.incDay = function() {
        day++;
        if (month <= 6) {
            if (day > 31) {
                this.incMonth();
                day = 1;
            }
        }
        else if (month < 12) {
            if (day > 30) {
                this.incMonth();
                day = 1;
            }
        }
        else {
            if (day > 29) {
                this.incMonth();
                day = 1;
            }
        }
    };

    this.incMonth = function() {
        month++;
        if (month > 12) {
            year++;
            month = 1;
        }
    };

    this.toString = function() {
        return year + "/" + this.twoDigit(month) + "/" + this.twoDigit(day) + " " + this.twoDigit(hour) + ":" + this.twoDigit(minute) + ":" + this.twoDigit(second);
    };

    this.twoDigit = function(no) {
        if (no < 10)
            return "0" + no;
        else
            return no;
    };

    this.getYear = function() {
        return year;
    };

    this.getMonth = function() {
        return month;
    };

    this.getDay = function() {
        return day;
    };

    this.getHour = function() {
        return hour;
    };

    this.getMinute = function() {
        return minute;
    };

    this.getSecond = function() {
        return second;
    };
}

var date;

function runClock(year, month, day, hour, minute, second) {
    date = new MyDate(year, month, day, hour, minute, second);
    window.setInterval(function() {
        date.incSec();
    }, 1000);
}

function assignToday2(event, dateInputId, timeInputId) {
    if (event.ctrlKey && event.keyCode == 32) {
        document.getElementById(dateInputId).value='';
        document.getElementById(timeInputId).value='';
    }
    else if (event.keyCode == 32) {
        date = new Date();
        var dateInput = document.getElementById(dateInputId);

        dateInput.value = (date.getYear() + 1900) + "/" + padZeroToDigit(date.getMonth()) + "/" + padZeroToDigit(date.getDay());

        if (timeInputId != null) {
            var timeInput = document.getElementById(timeInputId);
            var meridianType = $(timeInput).attr("meridianType");
            var hour = date.getHours();
            var meridianString = null;
            if(meridianType == "_12Hour") {
                meridianString = " AM";
                if(hour > 12) {
                    hour = hour - 12;
                    meridianString = " PM";
                }
            }

            timeInput.value = padZeroToDigit(hour) + ":" + padZeroToDigit(date.getMinutes()) + ":" + padZeroToDigit(date.getSeconds()) +
                (meridianString != null ? meridianString : "");
        }
    }
}

function assignToday(event, yearId, monthId, dayId, hourId, minuteId, secondId) {

    if (event.ctrlKey && event.keyCode == 32) {
        delAllCells(yearId, monthId, dayId, hourId, minuteId, secondId);
    }
    else if (event.keyCode == 32) {
        var year = document.getElementById(yearId);
        var month = document.getElementById(monthId);
        var day = document.getElementById(dayId);

        var hour = document.getElementById(hourId);

        year.value = date.getYear();
        month.value = date.getMonth();
        day.value = date.getDay();

        if (hour != null) {
            var minute = document.getElementById(minuteId);
            var second = document.getElementById(secondId);

            hour.value = date.getHour();
            minute.value = date.getMinute();
            second.value = date.getSecond();
        }
    }
}

function delAllCells(yearId, monthId, dayId, hourId, minuteId, secondId) {
    var year = document.getElementById(yearId);
    var month = document.getElementById(monthId);
    var day = document.getElementById(dayId);
    year.value = '';
    month.value = '';
    day.value = '';

    var hour = document.getElementById(hourId);
    if (hour != null) {
        var minute = document.getElementById(minuteId);
        var second = document.getElementById(secondId);
        hour.value = '';
        minute.value = '';
        second.value = '';
    }
}

function delTimeCells(hourId, minuteId, secondId) {
    var hour = document.getElementById(hourId);
    var minute = document.getElementById(minuteId);
    var second = document.getElementById(secondId);
    hour.value = '';
    minute.value = '';
    second.value = '';
}