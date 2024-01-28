#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include <ctime>
#include <cstdlib>
#include <iomanip>
#include <algorithm>
#include <cstdio>
#include "sha256.h"
using namespace std;

struct course {
	string courseID;
	int classNb;
	string courseName;
	string courseCredits;
	string prerequisite;
	int capacity;
	tm courseTime;

};

struct student {
	string id;
	string fName;
	string lName;
	string pass;
	string phone;
	string email;
	string uEmail;
	int nbCourses;
	course* c = new course[6];
};

int year() {
	time_t now = time(0);
	struct tm timeinfo;
	localtime_s(&timeinfo, &now); // localtime_r(&now, &timeinfo);  bgher matareh byemche ha bdel haydak
	int year = timeinfo.tm_year + 1900;
	return year;
}

bool name_alpha(string name) {
	for (int i = 0; i < name.length(); i++) {
		if (!isalpha(name[i]))
			return false;
	}
	return true;
}

bool pass_alpha(string pass) {
	int a = 0;
	for (int i = 0; i < pass.length(); i++) {
		if (isalpha(pass[i])) {
			a++;
			if (a > 0) {
				return true;
			}
		}
	}
	return false;
}

bool pass_num(string pass) {
	int n = 0;
	for (int i = 0; i < pass.length(); i++) {
		if (isdigit(pass[i])) {
			n++;
			if (n > 0)
				return true;
		}
	}
	return false;

}

bool pass_symbols(string pass) {
	for (int i = 0; i < pass.length(); i++) {
		if (!isalnum(pass[i])) {
			return true;
		}
	}
	return false;
}

bool passVerify(string pass) {
	if (pass.length() >= 8 && pass_alpha(pass) == true && pass_num(pass) == true && pass_symbols(pass) == true)
		return true;
	return false;
}

bool phoneNumberVerify(string phone) {
	if (phone.length() != 9)
		return false;
	for (int i = 0; i < 9; i++) {
		if (i == 2) {
			if (phone[i] != '-') {
				return false;
			}
		}
		else if (i != 2) {
			if (!isdigit(phone[i])) {
				return false;
			}
		}


	}
	if (phone[0] != '7' && phone[0] != '8' && phone[0] != '0') {
		return false;
	}
	if (phone[0] == '7') {
		if (phone[1] != '0' && phone[1] != '1' && phone[1] != '6' && phone[1] != '8' && phone[1] != '9') {
			return false;
		}
	}
	if (phone[0] == '8') {
		if (phone[1] != '1') {
			return false;
		}
	}
	return true;
}

bool emailVerify(string email) {
	int atPos = email.find('@');
	int dotPos = email.find_last_of('.');
	if (atPos > 0 && dotPos > atPos + 1 && dotPos < email.length() - 1)
		return true;
	return false;
}

string generateId() {

	string id;
	id += to_string(year());
	cout << "Are you willing to attend HHU in \n 1.fall \n 2.spring \n";
	int semesterNb;
	do {
		cout << "Please choose 1 or 2 :";
		cin >> semesterNb;
		if (semesterNb == 1)
			id += "01";
		else if (semesterNb == 2)
			id += "02";
	} while (semesterNb != 1 && semesterNb != 2);
	for (int i = 0; i < 3; i++) {
		int randomDigit = rand() % 10;
		id += to_string(randomDigit);
	}
	cout << "your Id is : " << id;
	return id;
}

bool checkId(string id) {
	fstream students_file;
	students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
	string idCheck;
	string emptyLine;
	while (students_file) {
		getline(students_file, idCheck, '\t');
		getline(students_file, emptyLine, '\n');

		if (idCheck == id) {
			return false;
		}
		return true;
	}
}

string generateUmail(string id) {
	string umail = id + "@hhu.edu.lb";
	return umail;
}

void signUp(student& user) {
	do {
		cout << "Enter your first name" << endl;
		cout << ">>";
		getline(cin, user.fName);
	} while (!name_alpha(user.fName));
	do {
		cout << "Enter your last name" << endl;
		cout << ">>";
		getline(cin, user.lName);
	} while (!name_alpha(user.lName));
	cout << "Create a password :";
	cout << endl;
	do {
		cout << "Your password must be at least 8 digits and contains numbers letters and special characters!" << endl;
		cout << ">>";
		getline(cin, user.pass);
	} while (passVerify(user.pass) == false);
	user.pass = sha256(user.pass);
	string pass2;
	do {
		cout << "Confirm your password: " << endl;
		cout << ">>";
		getline(cin, pass2);
		pass2 = sha256(pass2);
	} while (pass2 != user.pass);

	do {
		cout << "Enter your Lebanese phone number in the format XX-XXXXXX (you should include -):" << endl;
		cout << ">>";
		getline(cin, user.phone);
	} while (phoneNumberVerify(user.phone) == false);

	do {
		cout << "Enter your email:" << endl;
		cout << ">>";
		getline(cin, user.email);
	} while (emailVerify(user.email) == false);
	user.nbCourses = 0;

	user.id = generateId();
	if (checkId(user.id) == false)
		generateId();
	user.uEmail = generateUmail(user.id);
}

bool signIn(student& user, bool& sign) {
	fstream students_file;
	students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
	student testStudent;
	if (students_file.fail()) {
		cout << "Failed to connect to students file !" << endl;
	}
	while (!students_file.eof()) {
		getline(students_file, testStudent.id, ',');
		getline(students_file, testStudent.fName, ',');
		getline(students_file, testStudent.lName, ',');
		getline(students_file, testStudent.pass, ',');
		getline(students_file, testStudent.phone, ',');
		getline(students_file, testStudent.email, ',');
		getline(students_file, testStudent.uEmail, ',');
		students_file >> testStudent.nbCourses;
		students_file.ignore();

		if ((testStudent.id == user.id) && (testStudent.pass == sha256(user.pass))) {
			user = testStudent;
			sign = true;
			students_file.close();
			return true;
		}
	}
	sign = false;
	students_file.close();
	return false;
}

void setCourseTime(course& courseTest) {
	int option;
	
	do {
		cout << "What day of the week will it be presented ?\nEnter a nb between 0-6 as 1 is Monday and 5 is Friday \n" << ">>";
		cin >> option;
		courseTest.courseTime.tm_wday = option;
	} while (option < 0 || option>5);
	option = 0;
	do {
		cout << "\nChoose the timing :\n1. 8:30\n2. 11:30\n3. 3:30\n4. 6:30\n";
		cin >> option;
	} while (option != 1 && option != 2 && option != 3 && option != 4);
	switch (option) {
	case 1:
		courseTest.courseTime.tm_hour = 8;
		break;
	case 2:
		courseTest.courseTime.tm_hour = 11;
		break;
	case 3:
		courseTest.courseTime.tm_hour = 3;
		break;
	case 4:
		courseTest.courseTime.tm_hour = 6;
		break;
	}
	courseTest.courseTime.tm_min = 30;
	cout << "Enter the number of students in this class :" << endl << ">>";
	cin >> option;
	courseTest.capacity = option;
	do { //bel ghlt hatayton hon m b asser
		cout << "Does it need prerequisite?\n1.Yes\n2.No\n" << endl << ">>";
		cin >> option;
	} while (option != 1 && option != 2);
	cin.ignore();
	if (option == 1) {
		cout << "enter the prerequisite:" << endl << ">>";
		std::getline(cin, courseTest.prerequisite);
	}
}

int studentsNb() {
	fstream students_file;
	students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
	int lineCount = 0;
	string line;
	while (std::getline(students_file, line)) {
		++lineCount;
	}
	students_file.close();
	return lineCount;
}

student* allStudents = new student[studentsNb()];

void fillStudentsArr() {
	fstream students_file, registered_courses, courses_file;
	students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out );
	registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out );
	courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out );

	string line;
	string nothing;
	int lineCount = studentsNb();
	for (int i = 0; i < lineCount; i++) {
		getline(students_file, allStudents[i].id, ',');
		getline(students_file, allStudents[i].fName, ',');
		getline(students_file, allStudents[i].lName, ',');
		getline(students_file, allStudents[i].pass, ',');
		getline(students_file, allStudents[i].phone, ',');
		getline(students_file, allStudents[i].email, ',');
		getline(students_file, allStudents[i].uEmail, ',');
		getline(students_file, nothing, '\n');
		allStudents[i].nbCourses = stoi(nothing);
	}
	courses_file.close();
	registered_courses.close();
	students_file.close();
}

int courseFileLength() {
	fstream courses_file;
	courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out | ios::app);
	int lineCount = 0;
	string line;
	while (std::getline(courses_file, line)) {
		lineCount++;
	}
	courses_file.close();
	return lineCount;
}

course* allCourses = new course[courseFileLength()];

void fillCoursesArr() {
	string test;
	fstream courses_file;
	courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out );
	for (int i = 0; i < courseFileLength(); i++) {
		getline(courses_file,allCourses[i].courseID,',');
		getline(courses_file, test, ',');
		try {
			allCourses[i].classNb = stoi(test);
		}
		catch (const std::invalid_argument& e) {
			cout << "error here";
		}
		getline(courses_file, allCourses[i].courseName, ',');
		getline(courses_file, allCourses[i].courseCredits, ',');
		getline(courses_file, test, ',');
		if (test == "Monday") {
			allCourses[i].courseTime.tm_wday = 1;
		}
		else if (test == "Tuesday") {
			allCourses[i].courseTime.tm_wday = 2;
		}
		else if (test == "Wednesday") {
			allCourses[i].courseTime.tm_wday = 3;
		}
		else if (test == "Thursday") {
			allCourses[i].courseTime.tm_wday = 4;
		}
		else if (test == "Friday") {
			allCourses[i].courseTime.tm_wday = 5;
		}
		getline(courses_file, test, ',');
		int posof2pts = test.find(':');
		test = test.substr(0, posof2pts);
		try {
			allCourses[i].courseTime.tm_hour = stoi(test);
		}
		catch (const std::invalid_argument& e) {
			cout << "error here";
		}
		allCourses[i].courseTime.tm_min = 30;
		getline(courses_file,test,',');
		getline(courses_file, allCourses[i].prerequisite, ',');
		getline(courses_file,test , '\n');
		try { 
			allCourses[i].capacity = stoi(test);
		}
		catch (const std::invalid_argument& e) {
			cout << "error here";
		}
	}
	courses_file.close();
}


void sortCoursesByTime(course arr[], int n) {
	for (int i = 0; i < n - 1; i++) {
		for (int j = 0; j < n - i - 1; j++) {
			// Compare the days
			if (arr[j].courseTime.tm_wday > arr[j + 1].courseTime.tm_wday) {
				swap(arr[j], arr[j + 1]);
			}
			// If the days are the same, compare the hours
			else if (arr[j].courseTime.tm_wday == arr[j + 1].courseTime.tm_wday) {
				if (arr[j].courseTime.tm_hour > arr[j + 1].courseTime.tm_hour) {
					swap(arr[j], arr[j + 1]);
				}
			}
		}
	}
}
//mch sah

void studentArrToFile() {
	fstream students_file, temp;
	students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
	temp.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", ios::in | ios::out | ios::app);
	for (int i = 0; i < studentsNb(); i++) {
		temp << allStudents[i].id << ',' << allStudents[i].fName << ',' << allStudents[i].lName << ',' << allStudents[i].pass << ',' << allStudents[i].phone << ',' << allStudents[i].email << ',' << allStudents[i].uEmail << ',' << allStudents[i].nbCourses << '\n';
	}
	temp.close();
	students_file.close();
	remove("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv");
	rename("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", "C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv");

}

void drop(student signedStudent) {
	fstream students_file, registered_courses, courses_file;
	students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
	registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
	courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out | ios::app);
	string courseOption;
	//hot lschedule ya kalb hon
	while (!registered_courses.eof()) {
		getline(registered_courses, courseOption, '\n');
		int position = courseOption.find(',');
		if (courseOption.substr(position + 1) == signedStudent.id) {
			cout << courseOption.substr(0, position) << endl;
		}
	}
	int editCourse;
	cout << "Choose from your courses above a course to drop:(enter the Class Nb only)\nEnter 0 or any wrong nb to exit\n>>";
	cin >> editCourse;
	registered_courses.seekg(0);
	registered_courses.close();
	fstream temp("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", ios::out | ios::app);
	registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
	while (!registered_courses.eof()) {
		getline(registered_courses, courseOption, '\n');
		int pos = courseOption.find(',');
		if (courseOption.substr(0, pos) != to_string(editCourse)) {
			temp << courseOption << "\n";
		}
	}
	temp.close();
	registered_courses.close();
	remove("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv");
	rename("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", "C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv");
	for (int i = 0; i < studentsNb(); i++) {
		if (signedStudent.id == allStudents[i].id) {
			allStudents[i].nbCourses--;
		}
	}
	studentArrToFile();
}

int registeredCourseFileLength() {
	fstream registered_courses;
	registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
	int lineCount = 0;
	string line;
	while (std::getline(registered_courses, line)) {
		lineCount++;
	}
	registered_courses.close();
	return lineCount;
}

void fillEachStudentCourses(student student) {
	fstream registered_courses, courses_file;
	registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv",ios::in|ios::out);
	courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv",ios::in|ios::out);
	string classNb, id, line;
	string cId, cNb, cName, cCred, cDay, cHour, cEnd, cPre, cCap;
	int day, pos, k = 0;
	for (int i = 0; i < registeredCourseFileLength(); i++) {
		getline(registered_courses, classNb, ',');
		getline(registered_courses, id, '\n');
		if (id == student.id) {
			for (int j = 0; j < courseFileLength(); j++) {
				getline(courses_file, cId, ',');
				getline(courses_file, cNb, ',');
				getline(courses_file, cName, ',');
				getline(courses_file, cCred, ',');
				getline(courses_file, cDay, ',');
				if (cDay == "Monday") {
					day = 1;
				}
				else if (cDay == "Tuesday") {
					day = 2;
				}
				else if (cDay == "Wednesday") {
					day = 3;
				}
				else if (cDay == "Thursday") {
					day = 4;
				}
				else if (cDay == "Friday") {
					day = 5;
				}
				getline(courses_file, cHour, ',');
				pos = cHour.find(':');
				cHour = cHour.substr(0, pos);
				getline(courses_file, cEnd, ',');
				getline(courses_file, cPre, ',');
				getline(courses_file, cCap, '\n');
				if (cNb == classNb) {
					student.c[k].capacity = stoi(cCap);
					student.c[k].classNb = stoi(cNb);
					student.c[k].courseCredits = cCred;
					student.c[k].courseID = cId;
					student.c[k].courseName = cName;
					student.c[k].courseTime.tm_wday = day;
					student.c[k].courseTime.tm_hour = stoi(cHour);
					student.c[k].prerequisite = cPre;
					k++;
				}

			}
			courses_file.clear();
		}

	}
	courses_file.close();
	registered_courses.close();
}

void fillAllStudentsCourses() {
	for (int i = 0; i < studentsNb(); i++) {
		fillEachStudentCourses(allStudents[i]);
	}
}

bool checkCapacity(course c) {
	int cap = 0;
	string courseFound;
	fstream registered_courses;
	registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
	for (int i = 0; i < registeredCourseFileLength();i++) {
		getline(registered_courses,courseFound,'\n');
		int pos = courseFound.find(',');
		courseFound = courseFound.substr(0, pos-1);
		if (courseFound == to_string(c.classNb))
			cap++;
	}
	if (cap < c.capacity)
		return true;
	return false;
}

bool checkTimeConflict(course a, course b) {
	if (a.courseTime.tm_wday == b.courseTime.tm_wday) {
		if (a.courseTime.tm_hour == b.courseTime.tm_hour)
			return true; //true if there is conflict
	}
	return false;
}

void schedule(student stud) {
	string courseNb;
	string id;
	fstream registered_courses;
	registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
	
	for (int j = 0; j < studentsNb(); j++) {
		if (stud.id == allStudents[j].id) {

			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 1) {
					if (allStudents[j].c[i].courseTime.tm_hour == 8) {
						cout << allStudents[j].c[i].courseName << "\tMonday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 1) {
					if (allStudents[j].c[i].courseTime.tm_hour == 11) {
						cout << allStudents[j].c[i].courseName << "\tMonday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 1) {
					if (allStudents[j].c[i].courseTime.tm_hour == 3) {
						cout << allStudents[j].c[i].courseName << "\tMonday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 1) {
					if (allStudents[j].c[i].courseTime.tm_hour == 6) {
						cout << allStudents[j].c[i].courseName << "\tMonday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 2) {
					if (allStudents[j].c[i].courseTime.tm_hour == 8) {
						cout << allStudents[j].c[i].courseName << "\tTuesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 2) {
					if (allStudents[j].c[i].courseTime.tm_hour == 11) {
						cout << allStudents[j].c[i].courseName << "\tTuesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 2) {
					if (allStudents[j].c[i].courseTime.tm_hour == 3) {
						cout << allStudents[j].c[i].courseName << "\tTuesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 2) {
					if (allStudents[j].c[i].courseTime.tm_hour == 6) {
						cout << allStudents[j].c[i].courseName << "\tTuesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 3) {
					if (allStudents[j].c[i].courseTime.tm_hour == 8) {
						cout << allStudents[j].c[i].courseName << "\tWednesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30\t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 3) {
					if (allStudents[j].c[i].courseTime.tm_hour == 11) {
						cout << allStudents[j].c[i].courseName << "\tWednesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 3) {
					if (allStudents[j].c[i].courseTime.tm_hour == 3) {
						cout << allStudents[j].c[i].courseName << "\tWednesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 3) {
					if (allStudents[j].c[i].courseTime.tm_hour == 6) {
						cout << allStudents[j].c[i].courseName << "\Wednesday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 4) {
					if (allStudents[j].c[i].courseTime.tm_hour == 8) {
						cout << allStudents[j].c[i].courseName << "\tThursday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 4) {
					if (allStudents[j].c[i].courseTime.tm_hour == 11) {
						cout << allStudents[j].c[i].courseName << "\tThursday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 4) {
					if (allStudents[j].c[i].courseTime.tm_hour == 3) {
						cout << allStudents[j].c[i].courseName << "\tThursday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 4) {
					if (allStudents[j].c[i].courseTime.tm_hour == 6) {
						cout << allStudents[j].c[i].courseName << "\tThursday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 5) {
					if (allStudents[j].c[i].courseTime.tm_hour == 8) {
						cout << allStudents[j].c[i].courseName << "\tFriday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 5) {
					if (allStudents[j].c[i].courseTime.tm_hour == 11) {
						cout << allStudents[j].c[i].courseName << "\tFriday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 5) {
					if (allStudents[j].c[i].courseTime.tm_hour == 3) {
						cout << allStudents[j].c[i].courseName << "\tFriday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
			for (int i = 0; i < allStudents[j].nbCourses; i++) {
				if (allStudents[j].c[i].courseTime.tm_wday == 5) {
					if (allStudents[j].c[i].courseTime.tm_hour == 6) {
						cout << allStudents[j].c[i].courseName << "\tFriday\t" << allStudents[j].c[i].courseTime.tm_hour << ":30/t" << allStudents[j].c[i].courseTime.tm_hour + 3 << ":15\n";
					}
				}
			}
		}
	}	
}
int main() {
	fstream courses_file;
	string s;
	courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv",ios::out|ios::in);
	getline(courses_file, s, '\n');
	cout << s << "haha" << endl;
	courses_file.close();
	fillStudentsArr();
	fillCoursesArr();
	fillAllStudentsCourses();
	cout << allStudents[2].c[1].courseName << endl;
	char signOption;
	student signedStudent;
	student newStudent;
	int afterLoginOption;
	string courseOption;
	course newCourse;
	srand(static_cast<unsigned int>(time(0))); //because rand is always generating same nbs
	cout << "Welcome to HHUniveristy Student Information System ! ";
	cout << "\n \n";
	do {
		cout << "Please enter A or B from the list below :" << endl;
		cout << "A. Sign in \nB.Sign up(new student)" << endl;
		cout << ">>";
		cin >> signOption;
		cin.ignore();
	} while (signOption != 'a' && signOption != 'b' && signOption != 'A' && signOption != 'B');

	fstream students_file;
	students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
	switch (signOption) {
	case 'b':
	case 'B':
		signUp(newStudent);
		signedStudent = newStudent;
		students_file << signedStudent.id << ',' << signedStudent.fName << ',' << signedStudent.lName << ',' << signedStudent.pass << ',' << signedStudent.phone << ',' << signedStudent.email << ',' << signedStudent.uEmail << ',' << signedStudent.nbCourses << "\n";
		break;

	case 'a':
	case 'A':
		bool sign = false;
		do {
			students_file.clear();
			cout << "Enter your id :" << endl << ">>";
			getline(cin, signedStudent.id);
			cout << "Enter your password:" << endl << ">>";
			getline(cin, signedStudent.pass);
			if (signIn(signedStudent, sign)) {
				cout << "Welcome back " << signedStudent.fName << "!" << endl;
			}
		} while (sign == false);
		break;
	}
	students_file.close();

	do {
		if (!isalpha(signedStudent.uEmail[0])) {
			cout << "\nYou are a student.your privileges are limited\n\n";
			do {
				cout << "What are you willing to do :\n1.Add course\n2.Drop course\n3.Swap course\n4.Check schedule\n0.End\n" << endl;
				cin >> afterLoginOption;
			} while (afterLoginOption != 1 && afterLoginOption != 2 && afterLoginOption != 3 && afterLoginOption != 4 && afterLoginOption != 0);

			if (afterLoginOption == 1) {
				int conflicts = 0;
				string courseOption;
				string addCourse;
				fstream students_file, registered_courses, courses_file;
				course courseToAdd;
				registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
				courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out | ios::app);
				while (!courses_file.eof()) {
					getline(courses_file, courseOption, ','); 

					cout << courseOption << "\t";
				}
				courses_file.close();
				course testCourse;
				string test;
				cout << "Enter the course number of you are willing to add" << endl << ">>";
				cin >> addCourse;

				courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out | ios::app);
				while (!courses_file.eof()) {
					getline(courses_file, courseToAdd.courseID, ',');
					getline(courses_file, test, ',');
					try {
						courseToAdd.classNb = stoi(test);
					}
					catch (const std::invalid_argument& e) {
						cout << "error here";
					}
					getline(courses_file, courseToAdd.courseName, ',');
					getline(courses_file, courseToAdd.courseCredits, ',');
					getline(courses_file, test, ',');
					if (test == "Monday") {
						courseToAdd.courseTime.tm_wday = 1;
					}
					else if (test == "Tuesday") {
						courseToAdd.courseTime.tm_wday = 2;
					}
					else if (test == "Wednesday") {
						courseToAdd.courseTime.tm_wday = 3;
					}
					else if (test == "Thursday") {
						courseToAdd.courseTime.tm_wday = 4;
					}
					else if (test == "Friday") {
						courseToAdd.courseTime.tm_wday = 5;
					}
					getline(courses_file, test, ',');
					int posof2pts = test.find(':');
					test = test.substr(0, posof2pts);
					try {
						courseToAdd.courseTime.tm_hour = stoi(test);
					}
					catch (const std::invalid_argument& e) {
						cout << "error here";
					}
					courseToAdd.courseTime.tm_min = 30;
					getline(courses_file, test, ',');
					getline(courses_file, courseToAdd.prerequisite, ',');
					getline(courses_file, test, '\n');
					try {
						courseToAdd.capacity = stoi(test);
					}
					catch (const std::invalid_argument& e) {
						cout << "error here";
					}
					if (to_string(courseToAdd.classNb) == addCourse)
						break;
				}
				courses_file.close();
				for (int i = 0; i < courseFileLength(); i++) {
					if (to_string(allCourses[i].classNb) != addCourse) {
						if (checkTimeConflict(allCourses[i], courseToAdd) == true) {
							conflicts++;
						}
					}
					else
						conflicts++;
				}
				checkCapacity(courseToAdd); 
				if (checkCapacity(courseToAdd) == true && conflicts == 0 && signedStudent.nbCourses<6) { // zedet lnbcourses bala m jareb
					registered_courses << addCourse << ',' << signedStudent.id << '\n';
					registered_courses.close();
					for (int j = 0; j < studentsNb(); j++) {
						if (signedStudent.id == allStudents[j].id) {
							allStudents[j].nbCourses += 1;
							studentArrToFile();
						}
					}
				}
				else
					cout << "sorry you can't add this course\n\n" << endl;
				studentArrToFile();
			}
			else if (afterLoginOption == 2) {
				drop(signedStudent);
			}
			else if (afterLoginOption == 3) {
				fstream students_file, registered_courses, courses_file;
				course courseToAdd;
				string test;
				students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
				registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
				courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out | ios::app);
				string courseOption;
				cout << "check your schedule first :" << endl << endl;
				schedule(signedStudent);
				cout << "\n - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - _ - \n";
				while (!registered_courses.eof()) {
					getline(registered_courses, courseOption, '\n');
					int position = courseOption.find(',');
					if (courseOption.substr(position + 1) == signedStudent.id) {
						cout << courseOption.substr(0, position) << endl;
					}
				}
				int editCourse;
				cout << "Choose from your courses above a course to swap:(enter the Class Nb only)\nEnter 0 or any wrong nb to exit\n>>";
				cin >> editCourse;
				registered_courses.seekg(0);
				registered_courses.close();
				while (!courses_file.eof()) {
					getline(courses_file, courseOption, ',');

					cout << courseOption << "\t";
				}
				courses_file.close();
				int addCourse;
				cout << "Enter the course number of you are willing to replace your course" << endl << ">>";
				cin >> addCourse;
				courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out | ios::app);
				while (!courses_file.eof()) {
					getline(courses_file, courseToAdd.courseID, ',');
					getline(courses_file, test, ',');
					try {
						courseToAdd.classNb = stoi(test);
					}
					catch (const std::invalid_argument& e) {
						cout << "error here";
					}
					getline(courses_file, courseToAdd.courseName, ',');
					getline(courses_file, courseToAdd.courseCredits, ',');
					getline(courses_file, test, ',');
					if (test == "Monday") {
						courseToAdd.courseTime.tm_wday = 1;
					}
					else if (test == "Tuesday") {
						courseToAdd.courseTime.tm_wday = 2;
					}
					else if (test == "Wednesday") {
						courseToAdd.courseTime.tm_wday = 3;
					}
					else if (test == "Thursday") {
						courseToAdd.courseTime.tm_wday = 4;
					}
					else if (test == "Friday") {
						courseToAdd.courseTime.tm_wday = 5;
					}
					getline(courses_file, test, ',');
					int posof2pts = test.find(':');
					test = test.substr(0, posof2pts);
					try {
						courseToAdd.courseTime.tm_hour = stoi(test);
					}
					catch (const std::invalid_argument& e) {
						cout << "error here";
					}
					courseToAdd.courseTime.tm_min = 30;
					getline(courses_file, test, ',');
					getline(courses_file, courseToAdd.prerequisite, ',');
					getline(courses_file, test, '\n');
					try {
						courseToAdd.capacity = stoi(test);
					}
					catch (const std::invalid_argument& e) {
						cout << "error here";
					}
					if (courseToAdd.classNb == addCourse)
						break;
				}
				courses_file.close();
				//
				cout << addCourse << endl;
				cout << courseToAdd.classNb << endl;
				cout << editCourse << endl;
				int conflicts = 0;
				for (int i = 0; i < courseFileLength(); i++) {
					if (allCourses[i].classNb != addCourse) {
						if (checkTimeConflict(allCourses[i], courseToAdd) == true) {
							conflicts++;
						}
					}
					else
						conflicts++;
				}
				checkCapacity(courseToAdd);
				if (checkCapacity(courseToAdd) == true && conflicts == 0 && signedStudent.nbCourses < 6) { 
					registered_courses << addCourse << ',' << signedStudent.id << '\n';
					registered_courses.close();
					for (int j = 0; j < studentsNb(); j++) {
						if (signedStudent.id == allStudents[j].id) {
							studentArrToFile();
						}
					}
					fstream temp("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", ios::out | ios::app);
					registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv", ios::in | ios::out | ios::app);
					while (!registered_courses.eof()) {
						getline(registered_courses, courseOption, '\n');
						int pos = courseOption.find(',');
						if (courseOption.substr(0, pos) != to_string(editCourse)) {
							temp << courseOption << "\n";
						}
					}
					temp.close();
					registered_courses.close();
					remove("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv");
					rename("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", "C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\registered_courses.csv");


				}
				else
					cout << "sorry you can't add this course\n\n" << endl;
				studentArrToFile();

			}
			else if (afterLoginOption == 4) {
				schedule(signedStudent);
			}
		}

		if (isalpha(signedStudent.uEmail[0])) {
			cout << "\nWelcome to administrator mode\n\n";
			do {
				cout << "1.Add a course\n2.Remove a course\n3.Modify a course\n4.Add a new administrator\n0.End\n " << endl;
				cin >> afterLoginOption;
			} while (afterLoginOption != 1 && afterLoginOption != 2 && afterLoginOption != 3 && afterLoginOption != 0 && afterLoginOption != 4);

			string firstWord;
			string secondWord;
			string line;
			int oldNb = 0;
			fstream courses_file, temp;
			string day;
			int option, option2;
			string modifyOption;
			//ofstream courses_file2;
			switch (afterLoginOption) {
			case 1:
				courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out);
				while (courses_file) {
					getline(courses_file, line, ',');
					getline(courses_file, secondWord, ',');
					getline(courses_file, line, '\n');
				}
				courses_file.close();
				if (secondWord.empty()) {
					newCourse.classNb = 1;
				}
				else {
					oldNb = stoi(secondWord);
					newCourse.classNb = oldNb + 1;
				}                              
				cout << "Enter course id:" << endl << ">>";
				cin.ignore();
				getline(cin, newCourse.courseID);
				cout << "Enter course name:" << endl << ">>";
				getline(cin, newCourse.courseName);
				cout << "Enter the nb of credits:" << endl << ">>";
				getline(cin, newCourse.courseCredits);
				setCourseTime(newCourse);
				switch (newCourse.courseTime.tm_wday) {
				case 1:
					day = "Monday";
					break;
				case 2:
					day = "Tuesday";
					break;
				case 3:
					day = "Wednesday";
					break;
				case 4:
					day = "Thursday";
					break;
				case 5:
					day = "Friday";
					break;

				}
				courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out | ios::app);
				courses_file << newCourse.courseID << ',' << newCourse.classNb << ',' << newCourse.courseName << ',' << newCourse.courseCredits << ',' << day << ',' << newCourse.courseTime.tm_hour << ':' << newCourse.courseTime.tm_min << ',' << newCourse.courseTime.tm_hour + 3 << ':' << "15" << ',' << newCourse.prerequisite << ',' << newCourse.capacity << '\n';
				courses_file.close();
				break;
			case 2:
				for (int i = 0; i < courseFileLength(); i++) {
					switch (allCourses[i].courseTime.tm_wday) {
					case 1:
						day = "Monday";
						break;
					case 2:
						day = "Tuesday";
						break;
					case 3:
						day = "Wednesday";
						break;
					case 4:
						day = "Thursday";
						break;
					case 5:
						day = "Friday";
						break;

					}
					cout << i + 1 << ". " << allCourses[i].courseID << "\t" << allCourses[i].classNb << "\t" << allCourses[i].courseName << "\t" << day << "\t" << allCourses[i].courseTime.tm_hour << ":" << allCourses[i].courseTime.tm_min << '\n';
				}
				do {
					cout << "enter number of the line containing the course you are willing to remove\n>>";
					cin >> option;
				} while (option <= 0 && option > courseFileLength());
				for (option; option < courseFileLength(); option++) {
					allCourses[option - 1].capacity = allCourses[option].capacity;
					allCourses[option - 1].classNb = allCourses[option].classNb;
					allCourses[option - 1].courseCredits = allCourses[option].courseCredits;
					allCourses[option - 1].courseID = allCourses[option].courseID;
					allCourses[option - 1].courseName = allCourses[option].courseName;
					allCourses[option - 1].courseTime.tm_wday = allCourses[option].courseTime.tm_wday;
					allCourses[option - 1].courseTime.tm_hour = allCourses[option].courseTime.tm_hour;
					allCourses[option - 1].prerequisite = allCourses[option].prerequisite;
				}
				//courses_file2.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv",ios::out|ios::trunc);
				courses_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::trunc);
				courses_file.close();
				temp.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", ios::out | ios::app);
				for (int i = 0; i < courseFileLength()-1; i++) {
					switch (allCourses[i].courseTime.tm_wday) {
					case 1:
						day = "Monday";
						break;
					case 2:
						day = "Tuesday";
						break;
					case 3:
						day = "Wednesday";
						break;
					case 4:
						day = "Thursday";
						break;
					case 5:
						day = "Friday";
						break;

					}
					temp << allCourses[i].courseID << ',' << allCourses[i].classNb << ',' << allCourses[i].courseName << ',' << allCourses[i].courseCredits << ',' << day << ',' << allCourses[i].courseTime.tm_hour << ':' << allCourses[i].courseTime.tm_min << ',' << allCourses[i].courseTime.tm_hour + 3 << ':' << "15" << ',' << allCourses[i].prerequisite << ',' << allCourses[i].capacity << '\n';
				}
				courses_file.close();
				temp.close();
				remove("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv");
				rename("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", "C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv");
				break;
			case 3:
				for (int i = 0; i < courseFileLength(); i++) {
					switch (allCourses[i].courseTime.tm_wday) {
					case 1:
						day = "Monday";
						break;
					case 2:
						day = "Tuesday";
						break;
					case 3:
						day = "Wednesday";
						break;
					case 4:
						day = "Thursday";
						break;
					case 5:
						day = "Friday";
						break;

					}
					cout << i + 1 << ". " << allCourses[i].courseID << "\t" << allCourses[i].classNb << "\t" << allCourses[i].courseName << "\t" << day << "\t" << allCourses[i].courseTime.tm_hour << ":" << allCourses[i].courseTime.tm_min << '\n';
				}
				do {
					cout << "enter number of the line containing the course you are willing to remove\n>>";
					cin >> option;
				} while (option <= 0 && option > courseFileLength());
				option--; // laeno bet balech men 0
				do {
					cout << "are you willing to change:\n1.number of credits\n2.prerequisites\n3.day\n4.hour\n5.students capacity\n--enter the number corresponding\n>>";
					cin >> option2;
				} while (option2 < 1 || option2>5);
				switch (option2) {
				case 1:
					cout << "Enter the new number of credits\n>>";
					cin.ignore();
					getline(cin, modifyOption);
					allCourses[option].courseCredits = modifyOption;
					break;
				case 2:
					cout << "Enter new prerequisites or blank for no prerequisites\n>>";
					getline(cin, modifyOption);
					allCourses[option].prerequisite = modifyOption;
					break;
				case 3:
					do {
						cout << "1.Monday\n2.Tuesday\n3.Wednesday\n4.Thursday\n5.Friday\n>>";
						cin >> option2;
						cin.ignore();
					} while (option2 < 1 || option2>5);
					allCourses[option].courseTime.tm_wday = option2;
					break;
				case 4:
					do {
						cout << "Enter 8 for 8:30\nenter11 for 11:30\nenter 3 for 3:30\nenter 6 for 6:30\n>>";
						cin >> option2;
					} while (option2 != 3 && option2 != 6 && option2 != 11 && option2 != 8);
					allCourses[option].courseTime.tm_hour = option2;
					break;
				case 5:
					do{
					cout << "Enter the new capacity\n--Max 50 students--\n>>";
					cin >> option2;
					}while (option2 < 0||option2>50);
						allCourses[option].capacity = option2;
						break;
					}
				temp.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", ios::out | ios::app);
				for (int i = 0; i < courseFileLength(); i++) {
					switch (allCourses[i].courseTime.tm_wday) {
					case 1:
						day = "Monday";
						break;
					case 2:
						day = "Tuesday";
						break;
					case 3:
						day = "Wednesday";
						break;
					case 4:
						day = "Thursday";
						break;
					case 5:
						day = "Friday";
						break;

					}
					temp << allCourses[i].courseID << ',' << allCourses[i].classNb << ',' << allCourses[i].courseName << ',' << allCourses[i].courseCredits << ',' << day << ',' << allCourses[i].courseTime.tm_hour << ':' << allCourses[i].courseTime.tm_min << ',' << allCourses[i].courseTime.tm_hour + 3 << ':' << "15" << ',' << allCourses[i].prerequisite << ',' << allCourses[i].capacity << '\n';
				}
				courses_file.close();
				temp.close();
				remove("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv");
				rename("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp.csv", "C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv");
					break;
			case 4:
				student user;
				cin.ignore();
				srand(static_cast<unsigned int>(time(0)));
				do {
					cout << "Enter the first name" << endl;
					cout << ">>";
					getline(cin, user.fName);
				} while (!name_alpha(user.fName));
				do {
					cout << "Enter the last name" << endl;
					cout << ">>";
					getline(cin, user.lName);
				} while (!name_alpha(user.lName));
				cout << "Create a password :";
				cout << endl;
				do {
					cout << "His password must be at least 8 digits and contains numbers letters and special characters!" << endl;
					cout << ">>";
					getline(cin, user.pass);
				} while (passVerify(user.pass) == false);
				string pass2;
				do {
					cout << "Confirm your password: " << endl;
					cout << ">>";
					getline(cin, pass2);

				} while (pass2 != user.pass);

				do {
					cout << "Enter his Lebanese phone number in the format XX-XXXXXX (you should include -):" << endl;
					cout << ">>";
					getline(cin, user.phone);
				} while (phoneNumberVerify(user.phone) == false);

				do {
					cout << "Enter his email:" << endl;
					cout << ">>";
					getline(cin, user.email);
				} while (emailVerify(user.email) == false);
				user.nbCourses = 0;
				user.uEmail = user.fName + '.' + user.lName + "@hhu.edu.lb";
				user.id += to_string(year());
				user.id += "00";
				for (int i = 0; i < 3; i++) {
					int randomDigit = rand() % 10;
					user.id += to_string(randomDigit);
				}
				students_file.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\students.csv", ios::in | ios::out | ios::app);
				students_file << user.id << ',' << user.fName << ',' << user.lName << ',' << user.pass << ',' << user.phone << ',' << user.email << ',' << user.uEmail << ',' << user.nbCourses << "\n";
				students_file.close();
				break;
				}
			}
		} while (afterLoginOption != 0);

		fstream temp2, registered_courses;
		temp2.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\temp2.txt", ios::in | ios::out);
		registered_courses.open("C:\\Users\\Lenovo\\Desktop\\CCE\\prog 2\\course.csv", ios::in | ios::out);
		string classnb,id;
		for (int i = 0; i < courseFileLength(); i++) {
			for (int j = 0; j < registeredCourseFileLength(); j++) {
				getline(registered_courses,classnb,',');
				getline(registered_courses, id, '\n');
				if (to_string(allCourses[i].classNb) == classnb) {
					temp2 << classnb << '\t' << allCourses[i].courseName << '\t' << id << '\n';
				}
			}
		}
		temp2.close();
		registered_courses.close();
		return 0;
	}
