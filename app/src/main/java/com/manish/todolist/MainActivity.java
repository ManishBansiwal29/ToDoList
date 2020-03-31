package com.manish.todolist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final List<String> list = new ArrayList<>();
    int[] backgroundColors;
    int[] textColor;
    int[] selectedItemColor;
    boolean isSelectionEnabled;
    int[] selectionCell;
  //  int selectionCount=0;
    int selectionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       final ListView listView=findViewById(R.id.listView);
       final TextAdapter adapter = new TextAdapter();


       final int maxItems = 100;
       backgroundColors = new int[maxItems];
       textColor = new int[maxItems];
       selectedItemColor=new int[maxItems];
       selectionCell=new int[maxItems];
       for(int i=0;i<maxItems;i++){
           backgroundColors[i]=Color.LTGRAY;
           textColor[i]=Color.BLACK;
           selectionCell[i]=-1;
       }

        readInfo();

       for (int i=0;i<list.size();i++){
           if(list.get(i).startsWith("important")){
               backgroundColors[i]=Color.RED;
           }
       }

       adapter.setData(list,backgroundColors,textColor);
       listView.setAdapter(adapter);


       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
               if(isSelectionEnabled){
                   for (int i=0;i<selectionIndex;i++) {
                       if (selectionCell[i] == position) {
                           selectionCell[i] = -1;
                           selectedItemColor[position] = backgroundColors[position];
                           adapter.setData(list, selectedItemColor, textColor);
                           for (int j = 0; j < selectionIndex; j++) {
                               if (selectionCell[j] != -1) {
                                   return;
                               }

                           }

                           isSelectionEnabled = false;
                           findViewById(R.id.deleteSelectedTask).setVisibility(View.GONE);
                           adapter.setData(list, backgroundColors, textColor);
                           selectionIndex = 0;
                           return;
                       }
                   }
                   return;
               }
               AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                       .setTitle("Delete this Task")
                       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               list.remove(position);
                               for(int i=0;i<maxItems;i++){
                                   backgroundColors[i]=Color.LTGRAY;
                                   textColor[i]=Color.BLACK;
                               }
                               for (int i=0;i<list.size();i++){
                                   if(list.get(i).startsWith("important")){
                                       backgroundColors[i]=Color.RED;
                                   }
                               }
                               adapter.setData(list,backgroundColors,textColor);
                               saveInfo();
                           }
                       }).setNegativeButton("No",null)
                       .create();
               dialog.show();
           }
       });

       final Button deleteSelectedTask=findViewById(R.id.deleteSelectedTask);
       deleteSelectedTask.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               for(int i = 0; i<selectionIndex; i++){
                   if(selectionCell[i] != -1){
                       list.remove(selectionCell[i]);
                       selectionCell[i]=-1;
                   }
               }

               for(int i=0;i<maxItems;i++){
                   backgroundColors[i]=Color.LTGRAY;
                   textColor[i]=Color.BLACK;
               }
               for (int i=0;i<list.size();i++){
                   if(list.get(i).startsWith("important")){
                       backgroundColors[i]=Color.RED;
                   }
               }
               adapter.setData(list,backgroundColors,textColor);
               findViewById(R.id.deleteSelectedTask).setVisibility(View.GONE);
               isSelectionEnabled=false;
               selectionIndex=0;
               selectionCell[selectionIndex]=-1;
               saveInfo();
           }
       });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0;i<list.size();i++){
                    if(selectedItemColor[i]!=Color.YELLOW){
                        selectedItemColor[i]=backgroundColors[i];
                    }
                }
                isSelectionEnabled=true;
                selectionCell[selectionIndex]=position;
                selectionIndex++;
                selectedItemColor[position]=Color.YELLOW;
                adapter.setData(list,selectedItemColor,textColor);
                findViewById(R.id.deleteSelectedTask).setVisibility(View.VISIBLE);
                return true;
            }
        });

       final Button newTaskButton = findViewById(R.id.newTaskButton);
       newTaskButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               final EditText taskInput = new EditText(MainActivity.this);
               taskInput.setSingleLine();
               AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).
                       setTitle("Add A Task")
                       .setMessage("What is your new Task")
                       .setView(taskInput)
                       .setPositiveButton("Add a Task", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               String task ;
                               int taskCount = list.size();
                               task = taskInput.getText().toString();
                               if(task.startsWith("important")){
                                   list.add("");
                                   while (taskCount>0){
                                       list.set(taskCount,list.get(taskCount-1));
                                       backgroundColors[taskCount]=backgroundColors[taskCount-1];
                                       textColor[taskCount] = textColor[taskCount-1];
                                       taskCount--;
                                   }
                                   list.set(0,task);
                                   backgroundColors[0]=Color.RED;
                                   textColor[0]=Color.BLACK;
                               }else {
                                   list.add(task);
                               }
                               adapter.setData(list,backgroundColors,textColor);
                               saveInfo();
                           }
                       })
                       .setNegativeButton("Cancel",null)
                       .create();
               dialog.show();
           }
       });

       final Button deleteAllTasks = findViewById(R.id.deleteAllTaskButton);
       deleteAllTasks.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                       .setTitle("Are You Sure You want to Delete all Tasks")
                       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               list.clear();
                               for (int i=0;i<maxItems;i++){
                                   backgroundColors[i]=Color.LTGRAY;
                               }
                               adapter.setData(list,backgroundColors,textColor);
                               saveInfo();
                           }
                       }).setNegativeButton("No",null)
                       .create();
               dialog.show();

           }
       });



    }

    private void saveInfo(){

        try{
            File file = new File(this.getFilesDir(),"saved");
            FileOutputStream fOut = new FileOutputStream(file);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fOut));
            for (int i=0;i<list.size();i++){
                bw.write(list.get(i));
                bw.newLine();
            }
            bw.close();
            fOut.close();

    }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readInfo(){
        File file = new File(this.getFilesDir(),"saved");
        if(!file.exists()){
            return;
        }
            try {
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null){
                list.add(line);
                line = reader.readLine();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



class TextAdapter extends BaseAdapter {

    List<String> list = new ArrayList<>();
    int[] backgroundColors;
    int[] textColor;

    void setData(List<String> mList,int[] mBackgroundColors,int[] mTextColor){
        list.clear();
        list.addAll(mList);
        backgroundColors = new int[list.size()];
        textColor = new int[list.size()];
        for(int i=0; i<list.size();i++){
            backgroundColors [i] = mBackgroundColors[i];
            textColor [i] = mTextColor[i];
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.task);


        textView.setBackgroundColor(backgroundColors[position]);
        textView.setTextColor(textColor[position]);

        textView.setText(list.get(position));
        return convertView;
    }

}
}

