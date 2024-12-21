import React, { useState, useEffect } from 'react';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import './WeeklySchedule.css';

const localizer = momentLocalizer(moment);

const WeeklySchedule = () => {
  const [scheduleData, setScheduleData] = useState([]);

  const getDayOfWeekDate = (dayOfWeek) => {
    const currentDate = moment();
    const dayOfWeekIndex = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'].indexOf(dayOfWeek);
    
    const daysToAdd = (dayOfWeekIndex - currentDate.day() + 7) % 7;

    return currentDate.clone().add(daysToAdd, 'days').startOf('day');
  };

  const fetchEnrollments = async () => {
    try {
      const response = await fetch('/getClasses');
      const { courses, totalCredits } = await response.json();

      const events = courses.map(item => {
        const startDateTime = getDayOfWeekDate(item.day_Of_Week).set({
          hour: moment(item.time, 'HH:mm:ss').hour(),
          minute: moment(item.time, 'HH:mm:ss').minute(),
          second: moment(item.time, 'HH:mm:ss').second()
        });

        const endDateTime = moment(startDateTime).add(item.credits, 'hours');

        return {
          start: startDateTime.toDate(),
          end: endDateTime.toDate(),
          title: `${item.course_name} - ${item.credits} Credits`,
        };
      });

      setScheduleData(events);
    } catch (error) {
      console.error("Error fetching enrollments: ", error);
    }
  };

  useEffect(() => {
    fetchEnrollments();
  }, []);

  return (
    <div id='weekly_schedule'>
    <div className="schedule-container" id="weekly_schedule" style={{ height: '100%' }}>
      <Calendar
        localizer={localizer}
        events={scheduleData}
        views={['week']}
        defaultView="week"
        onSelectEvent={(event) => console.log(event)}
        style={{ height: '100%' }}
        step={60}
        timeslots={1}
        startAccessor="start"
        endAccessor="end"
        min={new Date().setHours(8, 0, 0, 0)}
        max={new Date().setHours(20, 0, 0, 0)} 
      />
    </div>
    </div>
  );
};

export default WeeklySchedule;
