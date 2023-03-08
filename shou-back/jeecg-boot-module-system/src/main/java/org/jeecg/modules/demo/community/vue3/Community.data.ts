import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
//列表数据
export const columns: BasicColumn[] = [
    {
    title: '小区id',
    align:"center",
    dataIndex: 'communityId'
   },
   {
    title: '经度',
    align:"center",
    dataIndex: 'longitude'
   },
   {
    title: '纬度',
    align:"center",
    dataIndex: 'latitude'
   },
   {
    title: '超时时间',
    align:"center",
    dataIndex: 'timeout'
   },
   {
    title: '待定2',
    align:"center",
    dataIndex: 'unknown2'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '小区id',
    field: 'communityId',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入小区id!'},
          ];
     },
  },
  {
    label: '经度',
    field: 'longitude',
    component: 'InputNumber',
  },
  {
    label: '纬度',
    field: 'latitude',
    component: 'InputNumber',
  },
  {
    label: '超时时间',
    field: 'timeout',
    component: 'InputNumber',
  },
  {
    label: '待定2',
    field: 'unknown2',
    component: 'Input',
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];
