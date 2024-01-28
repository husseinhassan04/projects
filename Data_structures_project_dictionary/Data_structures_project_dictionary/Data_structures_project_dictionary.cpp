#define _CRT_SECURE_NO_WARNINGS
#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
using namespace std;

struct sWord {
	char data[30];
	sWord* next;
};

struct dWord {
	char data[30];
	sWord* synonym, * antonym;
	dWord* next, * previous;
};

struct Dictionary {
	dWord* head, * tail;
};

void initialize(Dictionary* dict) {
	dict->head = NULL;
	dict->tail = NULL;
}

bool isEmpty(Dictionary* dict) {
	return(dict->head == NULL);
}

int dictSize() {
	ifstream inFile;
	inFile.open("C:\\Users\\Lenovo\\Desktop\\CCE\\data\\Data_structures_project_dictionary\\dictionary.txt");
	int lines = 0;
	string tmp;
	if (inFile.fail())
		cout << "File not found. Please try again." << endl;
	while (getline(inFile, tmp)) {
		lines++;
	}
	inFile.close();
	return lines;
}

//1
sWord* insertAtTailS(sWord* head, char* word) {
	sWord* tmp = new sWord;
	strcpy(tmp->data, word);
	tmp->next = NULL;
	if (head == NULL)
		return tmp;
	sWord* cur = head;
	while (cur->next != NULL) {
		cur = cur->next;
	}
	cur->next = tmp;
	return head;
}

//1
Dictionary* insertAtTailD(Dictionary* dict, string first, sWord* syn, sWord* anto) {
	dWord* tmp = new dWord;
	strcpy(tmp->data, first.c_str());
	tmp->synonym = syn;
	tmp->antonym = anto;
	tmp->next = NULL;

	if (isEmpty(dict)) {
		tmp->previous = NULL;
		dict->head = tmp;
		dict->tail = tmp;
	}

	else {
		tmp->previous = dict->tail;
		dict->tail->next = tmp;
		dict->tail = tmp;
	}
	return dict;
}

//1
Dictionary* insertLineToDictionary(Dictionary* dict, string line) {
	sWord* tmpSyn = nullptr;
	sWord* tmpAnto = nullptr;

	int posOf2Points = line.find(":");
	string firstSynonym = line.substr(0, posOf2Points);
	line = line.substr(posOf2Points + 1);

	while (!line.empty()) {
		int posOf2Points = line.find(":");
		if (posOf2Points == string::npos) {
			int posOfFirstHash = line.find("#");
			string lastSynonym = line.substr(0, posOfFirstHash);
			tmpSyn = insertAtTailS(tmpSyn, const_cast<char*>(lastSynonym.c_str()));
			line = line.substr(posOfFirstHash + 1);
			break;
		}
		string synonym = line.substr(0, posOf2Points);
		tmpSyn = insertAtTailS(tmpSyn, const_cast<char*>(synonym.c_str()));
		line = line.substr(posOf2Points + 1);
	}

	while (!line.empty()) {
		int posOfHashtag = line.find("#");
		if (posOfHashtag == string::npos) {
			string lastAntonym = line.substr(0);
			tmpAnto = insertAtTailS(tmpAnto, const_cast<char*>(lastAntonym.c_str()));
			break;
		}
		string antonym = line.substr(0, posOfHashtag);
		tmpAnto = insertAtTailS(tmpAnto, const_cast<char*>(antonym.c_str()));
		line = line.substr(posOfHashtag + 1);
	}

	dict = insertAtTailD(dict, firstSynonym, tmpSyn, tmpAnto);

	return dict;
}

//1
Dictionary* fillDictList(Dictionary* dictionary) {
	int lineCount = dictSize();

	ifstream inFile;
	inFile.open("C:\\Users\\Lenovo\\Desktop\\CCE\\data\\Data_structures_project_dictionary\\dictionary.txt");

	if (inFile.fail())
		cout << "File not found. Please try again." << endl;

	string line;
	for (int i = 0; i < lineCount; i++) {
		getline(inFile, line);
		dictionary = insertLineToDictionary(dictionary, line);
	}

	inFile.close();
	return dictionary;
}

//2
void fillFiles(Dictionary* dictionary) {
	int size = 0;
	dWord* test = new dWord;
	test = dictionary->head;
	while (test != nullptr) {
		size++;
		test = test->next;
	}
	ofstream emptyFile("C:\\Users\\Lenovo\\Desktop\\CCE\\data\\Data_structures_project_dictionary\\dictionary.txt");
	emptyFile.close(); // to empty the initial file

	ofstream outFile;
	outFile.open("C:\\Users\\Lenovo\\Desktop\\CCE\\data\\Data_structures_project_dictionary\\dictionary.txt");
	dWord* curD = dictionary->head;

	while (curD != NULL) {
		sWord* curSyn = curD->synonym;
		sWord* curAnto = curD->antonym;
		outFile << curD->data;
		while (curSyn != NULL) {

			outFile << ':' << curSyn->data;
			curSyn = curSyn->next;
		}
		while (curAnto != NULL) {
			outFile << "#" << curAnto->data;
			curAnto = curAnto->next;
		}
		if (size > 1) {
			outFile << endl;
			size--;
		}
		curD = curD->next;
	}
	outFile.close();
}

// kel chi mfrud 
void toLower(string& str) {
	for (int i = 0; i < str.length(); i++) {
		str[i] = tolower(str[i]);
	}
}

//3
dWord* merge(dWord* left, dWord* right) {
	if (left == nullptr) {
		return right;
	}

	if (right == nullptr) {
		return left;
	}

	if (strcmp(left->data, right->data) < 0) {
		left->next = merge(left->next, right);
		left->next->previous = left;
		left->previous = nullptr;
		return left;
	}
	else {
		right->next = merge(left, right->next);
		right->next->previous = right;
		right->previous = nullptr;
		return right;
	}
}

//3
dWord* mergeSort(dWord* head) {
	if (head == nullptr || head->next == nullptr) {
		return head;
	}

	dWord* slow = head;
	dWord* fast = head->next;

	while (fast != nullptr && fast->next != nullptr) {
		slow = slow->next;
		fast = fast->next->next;
	}

	dWord* left = head;
	dWord* right = slow->next;
	slow->next = nullptr;

	left = mergeSort(left);
	right = mergeSort(right);

	return merge(left, right);
}

//3
Dictionary* mergeSortDictionary(Dictionary* dict) {
	if (dict == nullptr || dict->head == nullptr || dict->head->next == nullptr) {
		return dict;
	}

	dict->head = mergeSort(dict->head);

	dWord* tmp = dict->head;
	while (tmp->next != nullptr) {
		tmp->next->previous = tmp;
		tmp = tmp->next;
	}

	return dict;
}

//4
Dictionary* addWord(Dictionary* dict, dWord* newWord) {
	dWord* tmp = dict->head;

	if (tmp == NULL) {
		dict->head = newWord;
		newWord->next = NULL;
		newWord->previous = NULL;
		return dict;
	}

	while (tmp != NULL) {
		if (strcmp(newWord->data, tmp->data) < 0) {
			if (tmp->previous == NULL) {
				newWord->previous = NULL;
				dict->head = newWord;
			}
			else {
				newWord->previous = tmp->previous;
				tmp->previous->next = newWord;
			}
			newWord->next = tmp;
			tmp->previous = newWord;
			return dict;
		}

		if (tmp->next == NULL) {
			tmp->next = newWord;
			newWord->previous = tmp;
			newWord->next = NULL;
			return dict;
		}

		tmp = tmp->next;
	}

	return dict;
}

//4
Dictionary* userAddWord(Dictionary* dictionary) {
	string principal;
	cout << "enter the principal word" << endl;
	getline(cin, principal);
	toLower(principal);
	dWord* tmp = dictionary->head;
	while (tmp != NULL) {
		if (strcmp(tmp->data, principal.c_str()) == 0) {
			cout << "Word already exists" << endl;
			return dictionary;
		}
		tmp = tmp->next;
	}
	sWord* synonyms = new sWord;
	synonyms = nullptr;
	sWord* antonyms = new sWord;
	antonyms = nullptr;

	string word;
	char wordC[30];
	int option1;
	cout << "do you want to add a synonym for the word?" << endl;
	cout << "1. yes\n 2. no" << endl;
	cin >> option1;
	if (option1 == 1) {
		cin.ignore();
		cout << "enter the synonym:" << endl;
		getline(cin, word);
		toLower(word);
		strcpy(wordC, word.c_str());
		/////
		dWord* tmpp = dictionary->head;
		int test = 0;
		while (tmpp != nullptr) {
			if (strcmp(tmpp->data, wordC)== 0) {
				test = 1;
			}
			tmpp = tmpp->next;
		}
		if (test == 0)//
			synonyms = insertAtTailS(synonyms, wordC);
		else {
			cout << "synonyms already exists in the dictonary" << endl;
			option1 = 0;
		}
	}
	int option2;
	cout << "do you want to add an antonym for the word?" << endl;
	cout << "1. yes\n 2. no" << endl;
	cin >> option2;
	if (option2 == 1) {
		cin.ignore();
		cout << "enter the antonym:" << endl;
		getline(cin, word);
		toLower(word);
		strcpy(wordC, word.c_str());
		antonyms = insertAtTailS(antonyms, wordC);
	}

	dWord* newWord = new dWord;
	strcpy(newWord->data, principal.c_str());
	newWord->synonym = synonyms;
	newWord->antonym = antonyms;
	dictionary = addWord(dictionary, newWord);

	char principalC[30];
	strcpy(principalC, principal.c_str());

	sWord* syn = synonyms;
	sWord* anto = antonyms;
	if (option1 == 1) {
		dWord* mix = new dWord;
		mix->synonym = nullptr;
		mix->antonym = nullptr;

		strcpy(mix->data, syn->data);
		mix->synonym = insertAtTailS(mix->synonym, principalC);
		if (anto != nullptr){
			mix->antonym = insertAtTailS(mix->antonym, anto->data);
			}
		dictionary = addWord(dictionary, mix);
	}
	if (option2 == 1) {
		dWord* antoAsPrincipal = new dWord;
		antoAsPrincipal->synonym = NULL;
		antoAsPrincipal->antonym = NULL;

		strcpy(antoAsPrincipal->data, anto->data); //aam yhot chi khaso bel deref null bs m bteemal mechkle
		antoAsPrincipal->antonym = insertAtTailS(antoAsPrincipal->antonym, principalC);
		if (syn != nullptr) {
			antoAsPrincipal->antonym = insertAtTailS(antoAsPrincipal->antonym, syn->data);
		}
		dictionary = addWord(dictionary, antoAsPrincipal);
	}

	cout << "\n\n\n...Done." << endl;
	return dictionary;
}

//5
Dictionary* addSynAnto(Dictionary* dict, string word1, string word2) { // string ashal lal user input
	dWord* cur = new dWord;
	cur = dict->head;
	char word1C[30];
	strcpy(word1C, word1.c_str());
	char word2C[30];
	strcpy(word2C, word2.c_str());
	if (cur == NULL) {
		cout << "use option 1 to add a word. (empty dictionary)" << endl;
		return dict;
	}
	while (cur->next != NULL) {
		if (strcmp(cur->data, word1C) == 0) {
			break;
		}
		cur = cur->next;
	}

	if (strcmp(cur->data, word1C) != 0) {
		cout << "use option 1 to add the word.(word not available in dictionary)" << endl;
		return dict;
	}
	int option;
	do {
		cout << "choose if the word you entered is \n1. synonym \n2. antonym" << endl;
		cin >> option;
	} while (option != 1 && option != 2);
	if (option == 1) {
		sWord* curS = new sWord;
		curS = cur->synonym;
		while (curS != NULL) {
			if (strcmp(curS->data, word2C) == 0) {
				cout << "synonym already exits" << endl;
				return dict;
			}
			curS = curS->next;
		}
		cur->synonym = insertAtTailS(cur->synonym, word2C);

		dWord* swapThem = new dWord;
		swapThem->synonym = nullptr;
		swapThem->antonym = nullptr;
		swapThem->previous = NULL;
		swapThem->next = NULL;
		strcpy(swapThem->data, word2C);

		swapThem->synonym = insertAtTailS(swapThem->synonym, word1C);
		sWord* curSyn = cur->synonym;
		while (curSyn->next != NULL) { //hatet next krml m terjaa thot lkelme synonym la hala
			swapThem->synonym = insertAtTailS(swapThem->synonym, curSyn->data);
			curSyn = curSyn->next;
		}

		sWord* curAnto = cur->antonym;	
		while (curAnto != NULL) {
			swapThem->antonym = insertAtTailS(swapThem->antonym, curAnto->data);
			curAnto = curAnto->next;
		}
		dict = addWord(dict, swapThem);
	}
	if (option == 2) {
		sWord* curA = new sWord;
		curA = cur->antonym;
		while (curA != NULL) {
			if (strcmp(curA->data, word2C) == 0) {
				cout << "antonym already exits" << endl;
				return dict;
			}
			curA = curA->next;
		}
		cur->antonym = insertAtTailS(cur->antonym, word2C);

		dWord* swapThem2 = new dWord;
		swapThem2->synonym = nullptr;
		swapThem2->antonym = nullptr;
		swapThem2->previous = NULL;
		swapThem2->next = NULL;

		strcpy(swapThem2->data, word2C);
		swapThem2->antonym = insertAtTailS(swapThem2->antonym, word1C);

		sWord* curSyn = cur->synonym;
		while (curSyn != NULL) {
			swapThem2->antonym = insertAtTailS(swapThem2->antonym, curSyn->data);
			curSyn = curSyn->next;
		}
		sWord* curAnto = cur->antonym;
		while (curAnto != NULL) {
			swapThem2->synonym = insertAtTailS(swapThem2->synonym, curAnto->data);
			curAnto = curAnto->next;
		}
		dict = addWord(dict, swapThem2);
	}

	cout << "\n\n\n...Done." << endl;
	return dict;
}

//6
bool containsLetters(string word, string letters) {
	return word.find(letters) != string::npos;
}

//6
sWord* deleteContainsS(sWord* word1, string word2) { //heye delete aadeye cheghel lcontain aamlo bel fct li tahet
	sWord* tmp = word1;
	sWord* prev = nullptr;
	while (tmp != nullptr) {
		if (strcmp(tmp->data, word2.c_str()) == 0) {
			if (prev == NULL) {
				word1 = tmp->next;
			}
			else {
				prev->next = tmp->next;

			}
			delete tmp;
			return word1;
		}
		prev = tmp;
		tmp = tmp->next;
	}
	return word1;
}

//6
Dictionary* deleteContainsUser(Dictionary* dict, string word) {
	dWord* tmp = dict->head;
	dWord* prev = nullptr;

	while (tmp != nullptr) {
		if (containsLetters(tmp->data, word)) {
			if (prev == nullptr) {
				dict->head = tmp->next;
				tmp->previous = NULL;
			}
			else if (tmp->next == nullptr) {
				prev->next = NULL;
			}
			else {
				prev->next = tmp->next;
				tmp->next->previous = prev;
			}
			dWord* tmpNext = tmp->next;
			////
			while (tmp->synonym) {
				sWord* tmppp = tmp->synonym;
				tmp->synonym = tmp->synonym->next;
				delete tmppp;
			}
			while (tmp->antonym) {
				sWord* tmppp = tmp->antonym;
				tmp->antonym = tmp->antonym->next;
				delete tmppp;
			}
			delete tmp;
			tmp = tmpNext;
		}
		else {
			sWord* tmpS = tmp->synonym;
			while (tmpS != nullptr) {
				if (containsLetters(tmpS->data, word)) {

					string str(tmpS->data);
					tmpS = tmpS->next;
					tmp->synonym = deleteContainsS(tmp->synonym, str);
					
				}
				else {
					tmpS = tmpS->next;
				}
			}
			sWord* tmpA = tmp->antonym;
			while (tmpA != nullptr) {
				if (containsLetters(tmpA->data, word)) {

					string str(tmpA->data);
					tmpA = tmpA->next;
					tmp->antonym = deleteContainsS(tmp->antonym, str);
					
				}
				else {
					tmpA = tmpA->next;
				}
			}
			prev = tmp;
			tmp = tmp->next;
		}

	}
	cout << "\n\n\n...Done." << endl;
	return dict;
}

//7 w 9
bool startWith(string word, string prefix) {
	if (word.length() < prefix.length()) {
		return false;
	}
	for (int i = 0; i < prefix.length(); i++) {
		if (word[i] != prefix[i])
			return false;
	}
	return true;
}

//7
void searchWordStart(Dictionary* dict, string word) {
	cout << ">>Word that start with " << word << "are:" << endl;
	dWord* cur = dict->head;
	while (cur != NULL) {
		if (startWith(cur->data, word))
			cout << cur->data << endl;
		cur = cur->next;
	}
	cout << "\n\n\n...Done." << endl;
}

//8
void searchWord(Dictionary* dict, string word) {
	dWord* cur = dict->head;
	char wordC[30];
	strcpy(wordC, word.c_str());
	while (cur != NULL) {
		if (strcmp(cur->data, wordC) == 0) {
			sWord* tmp = cur->synonym;
			cout << ">>>synonyms of this word are" << endl;
			while (tmp != nullptr) {
				cout << tmp->data << endl;
				tmp = tmp->next;
			}
			cout << "\n>>>antonyms of this word are" << endl;
			tmp = cur->antonym;
			while (tmp != nullptr) {
				cout << tmp->data << endl;
				tmp = tmp->next;
			}
			cout << "\n\n\n...Done." << endl;
			return;
		}
		cur = cur->next;
	}
	cout << "!!!Word doesn't exist in the dictionary" << endl;
}

//9
sWord* deleteStartWithS(sWord* word1, string word2) { //useless metel ldelete contains ken fine samiha delete w estaamela bel 2
	sWord* tmp = word1;
	sWord* prev = nullptr;
	while (tmp != nullptr) {
		if (strcmp(tmp->data, word2.c_str()) == 0) {
			if (prev == NULL) {
				word1 = tmp->next;
			}
			else {
				prev->next = tmp->next;

			}
			delete tmp;
			return word1;
		}
		prev = tmp;
		tmp = tmp->next;
	}
	cout << "\n\n\n...Done." << endl;
	return word1;
}

//9
Dictionary* deleteStartWithUser(Dictionary* dict, string word) {
	dWord* tmp = dict->head;
	dWord* prev = nullptr;

	while (tmp != nullptr) {
		if (startWith(tmp->data, word)) {
			if (prev == nullptr) {
				dict->head = tmp->next;
				tmp->previous = NULL;
			}
			else if (tmp->next == nullptr) {
				prev->next = NULL;
			}
			else {
				prev->next = tmp->next;
				tmp->next->previous = prev;
			}
			dWord* tmpNext = tmp->next;
			delete tmp;
			tmp = tmpNext;
		}
		else {
			sWord* tmpS = tmp->synonym;
			while (tmpS != nullptr) {
				if (startWith(tmpS->data, word)) {
					string str(tmpS->data);
					tmpS = tmpS->next; //
					tmp->synonym = deleteStartWithS(tmp->synonym, str);
					//break;
				}
				else {//
					tmpS = tmpS->next;
				}
			}
			sWord* tmpA = tmp->antonym;
			while (tmpA != nullptr) {
				if (startWith(tmpA->data, word)) {
					string str(tmpA->data);
					tmpA = tmpA->next; //
					tmp->antonym = deleteStartWithS(tmp->antonym, str);
					//break;
				}
				else {//
					tmpA = tmpA->next;
				}
			}
			prev = tmp;
			tmp = tmp->next;
		}

	}

	return dict;
}

int main() {

	Dictionary* dictionary = new Dictionary;
	initialize(dictionary);
	dictionary = fillDictList(dictionary);
	dictionary = mergeSortDictionary(dictionary);

	int option;
	cout << "Welcome to HH Dictionary" << endl;
	do {
		cout << "\n\n\n\nChoose an option to modify the dictionary:" << endl;
		cout << "1. add a new word" << endl;
		cout << "2. add a synonym/antonym for an existing word" << endl;
		cout << "3. delete all words containing some letters" << endl;
		cout << "4. search all words starting with" << endl;
		cout << "5. search for synonyms and antonyms of a word" << endl;
		cout << "6. delete all words starting with" << endl;
		cout << "0. to end!!" << endl;
		cin >> option;
		cin.ignore();
		if (option == 1) {
			dictionary = userAddWord(dictionary);
		}
		if (option == 2) {
			string principal, newWord;
			cout << "Enter the word you want to add a synonym/antonym to" << endl;
			getline(cin, principal);
			cout << "Enter the synonym or antonym" << endl;
			getline(cin, newWord);
			toLower(principal);
			toLower(newWord);
			dictionary = addSynAnto(dictionary, principal, newWord);
		}
		if (option == 3) {
			string letters;
			cout << " enter the letters you want to delete" << endl;
			getline(cin, letters);
			toLower(letters);
			dictionary = deleteContainsUser(dictionary, letters);
		}
		if (option == 4) {
			string letters;
			cout << " enter the letters" << endl;
			getline(cin, letters);
			toLower(letters);
			searchWordStart(dictionary, letters);
		}
		if (option == 5) {
			string word;
			cout << " enter a word to search for its synonyms and antonyms:";
			getline(cin, word);
			toLower(word);
			searchWord(dictionary, word);
		}
		if (option == 6) {
			string letters;
			cout << " enter the letters" << endl;
			getline(cin, letters);
			toLower(letters);
			dictionary = deleteStartWithUser(dictionary, letters);

		}
	} while (option != 0);

	fillFiles(dictionary);
	return 0;
}

/*demo sorted files
aweful:bad#nice#good
bad:aweful#nice#good
bye:see you#hi#hello#greetings
good:nice#bad#awful
greetings:hi:hello#bye#see you
hello:hi:greetings#bye#see you
hi:hello:greetings#bye#see you
nice:good#bad#awful
see you:bye#hi#hello#greetings
*/

/* unsorted test
hi:hello:greetings#bye#see you
hello:hi:greetings#bye#see you
greetings:hi:hello#bye#see you
bye:see you#hi#hello#greetings
see you:bye#hi#hello#greetings
good:nice#bad#awful
nice:good#bad#awful
bad:aweful#nice#good
aweful:bad#nice#good
*/

