package com.hust.adapter;

import java.util.ArrayList;
import java.util.List;

import com.hust.model.MainListItem;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hust.activity.R;
import com.hust.common.Constant;

public class TaskAdapter extends
		RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

	private List<MainListItem> items;

	public TaskAdapter(List<MainListItem> items) {
		super();
		this.items = items;
		listSelectItems=new ArrayList<>();
	}

	private List<MainListItem> listSelectItems;

	public List<MainListItem> getListMainItemListSelected(){
		return listSelectItems;
	}

	public void toggleSelection(int pos) {
		if(Constant.listProcessSelected.contains(items.get(pos).processName)){
			Constant.listProcessSelected.remove(items.get(pos).processName);
			listSelectItems.remove(items.get(pos));
		}else{
			Constant.listProcessSelected.add(items.get(pos).processName);
			listSelectItems.add(items.get(pos));
		}

		notifyItemChanged(pos);
	}

	public void clearSelections() {
		Constant.listProcessSelected.clear();
		listSelectItems.clear();
		notifyDataSetChanged();
	}

	public int getSelectedItemCount() {
		return Constant.listProcessSelected.size();
	}

	public List<String> getSelectedItems() {
		return Constant.listProcessSelected;
	}

	TaskClickListener listener;

	public TaskClickListener getListener() {
		return listener;
	}

	public void setListener(TaskClickListener listener) {
		this.listener = listener;
	}

	class TaskViewHolder extends RecyclerView.ViewHolder implements
			OnClickListener {

		ImageView imvIcon;
		TextView tvProcessName, tvMemory, tvPercent, tvType;

		public TaskViewHolder(View v) {
			super(v);
			imvIcon = (ImageView) v.findViewById(R.id.imvIcon);
			tvProcessName = (TextView) v.findViewById(R.id.tvProcessName);
			tvMemory = (TextView) v.findViewById(R.id.tvMemory);
	//		tvPercent = (TextView) v.findViewById(R.id.tvPercent);
			tvType = (TextView) v.findViewById(R.id.tvType);
			v.setOnClickListener(this);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onClick(View v) {
			listener.onItemClick(getAdapterPosition(), v,
					items.get(getAdapterPosition()));
		}
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public void onBindViewHolder(TaskViewHolder holder, int pos) {
		MainListItem item = items.get(pos);
		holder.imvIcon.setImageDrawable(item.icon);
		holder.tvProcessName.setText(item.label);
		holder.tvMemory.setText(item.memory + " kB ,");
	//	holder.tvPercent.setText(item.cpu + "%, ");
		holder.tvType.setText(kind(item.importance));
		if(item.importance==0){
			holder.tvType.setTextColor(Color.RED);
		}else{
			holder.tvType.setTextColor(Color.BLUE);
		}
		holder.itemView.setActivated(Constant.listProcessSelected.contains(item.processName));

	}

	private String kind(int importance) {
		String str = "";
		if (importance == 100)
			str = "foreground";
		else if (importance == 130)
			str = "activity";
		else if (importance == 200)
			str = "activity";
		else if (importance == 300)
			str = "service";
		else if (importance == 400)
			str = "background";
		else if (importance == 500)
			str = "empty";
		else if(importance==0)
			str="stopped";
		return str;
	}

	@Override
	public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_list, parent, false);
		return new TaskViewHolder(v);
	}

	public interface TaskClickListener {
		void onItemClick(int position, View v, MainListItem item);
	}

}
